package com.back.service;

import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Correo no encontrado: " + email));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("Cuenta no activada. Revisa tu email para activar la cuenta.");
        }

    return User.builder()
            .username(usuario.getEmail())
            .password(usuario.getClave())
            .authorities(new SimpleGrantedAuthority(usuario.getRol()))
            .build();
    }
}