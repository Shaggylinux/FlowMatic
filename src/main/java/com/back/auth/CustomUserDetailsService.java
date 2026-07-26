package com.back.auth;

import com.back.shared.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(email)) {
            throw new UsernameNotFoundException("Cuenta bloqueada temporalmente");
        }

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !usuario.isActivo()) {
            throw new UsernameNotFoundException("Credenciales invalidas");
        }

    return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getClave())
            .authorities(new SimpleGrantedAuthority(usuario.getRol()))
            .build();
    }
}