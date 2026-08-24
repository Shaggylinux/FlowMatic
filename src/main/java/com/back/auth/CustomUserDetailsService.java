package com.back.auth;

import com.back.seguridad.LoginAttemptService;

import org.springframework.security.authentication.LockedException;
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
            throw new LockedException("Cuenta bloqueada temporalmente por intentos fallidos");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado).orElse(null);

        if (usuario == null) {
            throw new UsernameNotFoundException("Credenciales inválidas");
        }

        if (usuario.isBloqueado()) {
            throw new LockedException("Cuenta bloqueada por un administrador");
        }

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getClave())
                .disabled(!usuario.isActivo())
                .authorities(new SimpleGrantedAuthority(usuario.getRol()))
                .build();
    }
}