package com.back.seguridad;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginAttemptService loginAttemptService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, LoginFailureHandler loginFailureHandler,
            LoginSuccessHandler loginSuccessHandler) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/seed"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registro/**", "/login", "/error", "/css/**", "/forgot-password",
                                "/reset-password", "/js/**", "/home", "/", "/api/seed", "/videos/**")
                        .permitAll()
                        .requestMatchers("/candidato/**").hasRole("CANDIDATO")
                        .requestMatchers("/calendario/**").hasAnyRole("RRHH", "CANDIDATO")
                        .requestMatchers("/gestion-candidatos/**").hasAnyRole("RRHH", "ADMINISTRADOR")
                        .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/drive/**").hasAnyRole("RRHH", "CANDIDATO")
                        .requestMatchers("/rrhh/**", "/subir-archivo", "/crear-carpeta", "/eliminar", "/descargar",
                                "/drive/ver-archivo/**")
                        .hasAnyRole("RRHH", "CANDIDATO")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll());
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}