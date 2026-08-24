package com.back.seguridad;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.shared.event.AuditoriaEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final ApplicationEventPublisher eventPublisher;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        if (email != null && !email.isBlank()) {
            String emailNormalizado = email.trim();
            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado).orElse(null);

            if (usuario != null) {
                if (usuario.isBloqueado() || loginAttemptService.isBlocked(emailNormalizado) || exception instanceof LockedException || (exception.getCause() instanceof LockedException)) {
                    getRedirectStrategy().sendRedirect(request, response, "/login?bloqueado");
                    return;
                }

                if (!usuario.isActivo() || exception instanceof DisabledException || (exception.getCause() instanceof DisabledException)) {
                    getRedirectStrategy().sendRedirect(request, response, "/login?inactiva");
                    return;
                }
            } else {
                if (loginAttemptService.isBlocked(emailNormalizado)) {
                    getRedirectStrategy().sendRedirect(request, response, "/login?bloqueado");
                    return;
                }
            }

            loginAttemptService.recordFailed(emailNormalizado);
            eventPublisher.publishEvent(new AuditoriaEvent(this, "SEGURIDAD",
                "Intento de inicio de sesión fallido: " + emailNormalizado,
                emailNormalizado, "SEGURIDAD"));

            if (loginAttemptService.isBlocked(emailNormalizado)) {
                loginAttemptService.publicarEventoBloqueo(emailNormalizado);
                getRedirectStrategy().sendRedirect(request, response, "/login?bloqueado");
                return;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}
