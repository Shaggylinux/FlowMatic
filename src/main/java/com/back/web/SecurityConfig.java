package com.back.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) 
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/registro/**", "/login", "/error", "/css/**", "/js/**").permitAll() 
<<<<<<< HEAD
            .requestMatchers("/", "/subir-archivo", "/crear-carpeta", "/compartir", "/eliminar", "/descargar").authenticated()
            .requestMatchers("/guardar", "/Formulario", "/editar").hasRole("ADMIN")
=======
            .requestMatchers("/guardar", "/eliminar", "/Formulario", "/editar").hasRole("ADMIN")
            .requestMatchers("/", "/detalle").authenticated()
>>>>>>> c58474b2d4e77f8e63c05950c2f64bca7b9a88d2
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll()
<<<<<<< HEAD
        );
=======
        )
        .csrf(csrf -> csrf.disable());
    
>>>>>>> c58474b2d4e77f8e63c05950c2f64bca7b9a88d2
    return http.build();
    }
}