package com.back.seguridad;

import org.springframework.context.ApplicationEventPublisher;
import com.back.shared.event.AuditoriaEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        if (email != null) {
            if (loginAttemptService.isBlocked(email) || exception instanceof LockedException) {
                getRedirectStrategy().sendRedirect(request, response, "/login?bloqueado");
                return;
            }
            if (exception instanceof DisabledException) {
                getRedirectStrategy().sendRedirect(request, response, "/login?inactiva");
                return;
            }
            loginAttemptService.recordFailed(email);
            eventPublisher.publishEvent(new AuditoriaEvent(this, "SEGURIDAD",
                "Intento de inicio de sesión fallido: " + email,
                email, "SEGURIDAD"));
            if (loginAttemptService.isBlocked(email)) {
                loginAttemptService.publicarEventoBloqueo(email);
                getRedirectStrategy().sendRedirect(request, response, "/login?bloqueado");
                return;
            }
        }
        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}
