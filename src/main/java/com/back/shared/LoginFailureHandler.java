package com.back.shared;

import com.back.admin.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final LoginAttemptService loginAttemptService;
    private final AuditoriaService auditoriaService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String email = request.getParameter("email");
        if (email != null) {
            if (loginAttemptService.isBlocked(email)) {
                getRedirectStrategy().sendRedirect(request, response, "/login?bloqueado");
                return;
            }
            loginAttemptService.recordFailed(email);
            auditoriaService.registrar("SEGURIDAD",
                "Intento de inicio de sesi\u00f3n fallido: " + email,
                email, "SEGURIDAD");
        }
        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}
