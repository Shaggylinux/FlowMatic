package com.back.controller;

import com.back.model.Usuario;
import com.back.repository.UsuarioRepository;
import com.back.service.FilesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SeedController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private FilesServices filesServices;

    @PostMapping("/seed")
    public ResponseEntity<?> seed() {
        List<Map<String, Object>> created = new ArrayList<>();

        // Create RRHH user if it doesn't exist
        if (usuarioRepository.findByEmail("rrhh@flowmatic.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setUsername("Admin");
            admin.setApellido("RRHH");
            admin.setEmail("rrhh@flowmatic.com");
            admin.setClave(passwordEncoder.encode("Admin1234"));
            admin.setRol("ROLE_RRHH");
            admin.setActivo(true);
            admin.setUltimaActualizacion(LocalDateTime.now());
            usuarioRepository.save(admin);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "rrhh");
            m.put("email", "rrhh@flowmatic.com");
            created.add(m);
        }

        // Create ADMINISTRADOR user if it doesn't exist
        if (usuarioRepository.findByEmail("admin@sistema.com").isEmpty()) {
            Usuario adm = new Usuario();
            adm.setUsername("Super");
            adm.setApellido("Admin");
            adm.setEmail("admin@sistema.com");
            adm.setClave(passwordEncoder.encode("Admin1234"));
            adm.setRol("ROLE_ADMINISTRADOR");
            adm.setActivo(true);
            adm.setUltimaActualizacion(LocalDateTime.now());
            usuarioRepository.save(adm);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "admin");
            m.put("email", "admin@sistema.com");
            created.add(m);
        }

        // Create candidate test data if none exist
        long candidateCount = usuarioRepository.findByRol("ROLE_CANDIDATO").size();
        if (candidateCount == 0) {
            String[][] data = {
                {"Ana María", "López", "ana.lopez@email.com", "Desarrolladora Full Stack", "Bogotá", "Java,Spring Boot,React,PostgreSQL,Docker", "Español,Inglés,Francés", "5", "Inmediata", "Disponible"},
                {"Carlos", "Martínez", "carlos.martinez@email.com", "DevOps Engineer", "Medellín", "Docker,Kubernetes,AWS,Terraform,Jenkins", "Español,Inglés", "7", "Inmediata", "Disponible"},
                {"María", "García", "maria.garcia@email.com", "UX/UI Designer", "Cali", "Figma,Adobe XD,Sketch,InVision,Photoshop", "Español,Inglés", "4", "2 semanas", "En proceso"},
                {"Pedro", "Rodríguez", "pedro.rodriguez@email.com", "Backend Developer", "Barranquilla", "Java,Spring Boot,Python,Django,MySQL", "Español,Inglés", "3", "1 mes", "Entrevista"},
                {"Laura", "Hernández", "laura.hernandez@email.com", "Project Manager", "Bogotá", "Jira,Confluence,Scrum,Agile,Slack", "Español,Inglés,Portugués", "8", "Inmediata", "Disponible"},
                {"Diego", "Ramírez", "diego.ramirez@email.com", "Data Scientist", "Medellín", "Python,TensorFlow,PyTorch,SQL,Spark", "Español,Inglés", "6", "1 mes", "Seleccionado"},
                {"Sofía", "Torres", "sofia.torres@email.com", "Frontend Developer", "Bogotá", "React,Vue.js,TypeScript,Tailwind,Next.js", "Español,Inglés", "4", "Inmediata", "Disponible"},
                {"Andrés", "Castro", "andres.castro@email.com", "Ingeniero de Sistemas", "Pereira", "Java,Python,SQL,Linux,Redes", "Español", "2", "2 semanas", "Registrado"},
            };

            for (String[] row : data) {
                if (usuarioRepository.findByEmail(row[2]).isPresent()) continue;
                Usuario u = new Usuario();
                u.setUsername(row[0]);
                u.setApellido(row[1]);
                u.setEmail(row[2]);
                u.setClave(passwordEncoder.encode("Test1234"));
                u.setRol("ROLE_CANDIDATO");
                u.setActivo(true);
                u.setCargo(row[3]);
                u.setCiudad(row[4]);
                u.setTecnologias(row[5]);
                u.setIdiomas(row[6]);
                u.setExperiencia(Integer.parseInt(row[7]));
                u.setDisponibilidad(row[8]);
                u.setEstado(row[9]);
                u.setUltimaActualizacion(LocalDateTime.now());
                usuarioRepository.save(u);
                filesServices.crearCarpetaCandidato(row[2]);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("type", "candidato");
                m.put("email", row[2]);
                created.add(m);
            }
        }

        return ResponseEntity.ok(Map.of(
            "created", created.size(),
            "total", usuarioRepository.count(),
            "users", created
        ));
    }
}
