package com.back.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean

    public BCryptPasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registro/**", "/login", "/error", "/css/**", "/forgot-password",
                                "/reset-password", "/js/**", "/home", "/videos/**", "/")
                        .permitAll()
                        .requestMatchers("/candidato/**").hasRole("CANDIDATO")
                        .requestMatchers("/rrhh/**", "/subir-archivo", "/crear-carpeta", "/eliminar", "/descargar")
                        .hasRole("RRHH")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("clave")
                        .defaultSuccessUrl("/post-login", true)
                        .permitAll());

        return http.build();
    }
}