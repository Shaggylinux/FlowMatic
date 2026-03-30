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
        // 1. DESACTIVAR CSRF para que los POST (eliminar, compartir, subir) funcionen directo
        .csrf(csrf -> csrf.disable()) 
        
        .authorizeHttpRequests(auth -> auth
            // Rutas públicas
            .requestMatchers("/registro/**", "/login", "/error", "/css/**", "/js/**").permitAll() 
            
            // Rutas que requieren estar logueado (sin importar si es ADMIN o USER)
            .requestMatchers("/", "/subir-archivo", "/crear-carpeta", "/compartir", "/eliminar", "/detalle").authenticated()
            
            // Rutas exclusivas de ADMIN (si las sigues usando para otra cosa)
            .requestMatchers("/guardar", "/Formulario", "/editar").hasRole("ADMIN")
            
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
        );
    
    return http.build();
}
}