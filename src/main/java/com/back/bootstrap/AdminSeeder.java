package com.back.bootstrap;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.admin.Administrador;
import com.back.admin.AdministradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@flowmatic.com";
        
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario u = new Usuario();
            u.setEmail(adminEmail);
            // La contraseña es "admin123"
            u.setClave(new BCryptPasswordEncoder().encode("admin123"));
            u.setRol("ROLE_ADMINISTRADOR");
            u.setActivo(true);
            Usuario savedUsuario = usuarioRepository.save(u);

            Administrador a = new Administrador();
            a.setId(savedUsuario.getId());
            a.setUsername("Admin");
            a.setApellido("FlowMatic");
            administradorRepository.save(a);

            System.out.println("=========================================================");
            System.out.println("[Seeder] Usuario administrador maestro creado con éxito:");
            System.out.println("[Seeder] Email: " + adminEmail);
            System.out.println("[Seeder] Clave: admin123");
            System.out.println("=========================================================");
        }
    }
}
