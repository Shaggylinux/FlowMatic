package com.back.auth;

import com.back.seguridad.LoginAttemptService;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email != null ? email.trim() : "";
        if (loginAttemptService.isBlocked(emailNormalizado)) {
            throw new UsernameNotFoundException("Cuenta bloqueada temporalmente");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado).orElse(null);

        if (usuario == null || !usuario.isActivo()) {
            throw new UsernameNotFoundException("Credenciales invalidas");
        }

        if (usuario.isBloqueado()) {
            throw new UsernameNotFoundException("Cuenta bloqueada por un administrador");
        }

    return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getClave())
            .authorities(new SimpleGrantedAuthority(usuario.getRol()))
            .build();
    }
}