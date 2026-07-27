package com.back.shared;

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
        }
        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}
