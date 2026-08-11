package com.back.bootstrap;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.admin.Administrador;
import com.back.admin.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@flowmatic.com";

        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario u = new Usuario();
            u.setEmail(adminEmail);
            u.setClave(new BCryptPasswordEncoder().encode("admin123*"));
            u.setRol("ROLE_ADMINISTRADOR");
            u.setActivo(true);
            Usuario savedUsuario = usuarioRepository.save(u);

            Administrador a = new Administrador();
            a.setId(savedUsuario.getId());
            a.setUsername("Admin");
            a.setApellido("FlowMatic");
            administradorRepository.save(a);

            log.info("[Seeder] Usuario administrador maestro creado con éxito: {}", adminEmail);
        }
    }
}
