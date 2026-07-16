package com.back.controller;

import com.back.model.Usuario;
import com.back.model.Candidato;
import com.back.model.RRHH;
import com.back.repository.UsuarioRepository;
import com.back.repository.CandidatoRepository;
import com.back.repository.RRHHRepository;
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
    private CandidatoRepository candidatoRepository;

    @Autowired
    private RRHHRepository rrhhRepository;

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
            admin.setEmail("rrhh@flowmatic.com");
            admin.setClave(passwordEncoder.encode("Admin1234"));
            admin.setRol("ROLE_RRHH");
            admin.setActivo(true);
            admin = usuarioRepository.save(admin);

            RRHH rrhh = new RRHH();
            rrhh.setId(admin.getId());
            rrhh.setUsername("Admin");
            rrhh.setApellido("RRHH");
            rrhhRepository.save(rrhh);

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", "rrhh");
            m.put("email", "rrhh@flowmatic.com");
            created.add(m);
        }

        // Create ADMINISTRADOR user if it doesn't exist
        if (usuarioRepository.findByEmail("admin@sistema.com").isEmpty()) {
            Usuario adm = new Usuario();
            adm.setEmail("admin@sistema.com");
            adm.setClave(passwordEncoder.encode("Admin1234"));
            adm.setRol("ROLE_ADMINISTRADOR");
            adm.setActivo(true);
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
                {"Ana Mar\u00eda", "L\u00f3pez", "ana.lopez@email.com", "Desarrolladora Full Stack", "Bogot\u00e1", "Java,Spring Boot,React,PostgreSQL,Docker", "Espa\u00f1ol,Ingl\u00e9s,Franc\u00e9s", "5", "Inmediata", "Disponible"},
                {"Carlos", "Mart\u00ednez", "carlos.martinez@email.com", "DevOps Engineer", "Medell\u00edn", "Docker,Kubernetes,AWS,Terraform,Jenkins", "Espa\u00f1ol,Ingl\u00e9s", "7", "Inmediata", "Disponible"},
                {"Mar\u00eda", "Garc\u00eda", "maria.garcia@email.com", "UX/UI Designer", "Cali", "Figma,Adobe XD,Sketch,InVision,Photoshop", "Espa\u00f1ol,Ingl\u00e9s", "4", "2 semanas", "En proceso"},
                {"Pedro", "Rodr\u00edguez", "pedro.rodriguez@email.com", "Backend Developer", "Barranquilla", "Java,Spring Boot,Python,Django,MySQL", "Espa\u00f1ol,Ingl\u00e9s", "3", "1 mes", "Entrevista"},
                {"Laura", "Hern\u00e1ndez", "laura.hernandez@email.com", "Project Manager", "Bogot\u00e1", "Jira,Confluence,Scrum,Agile,Slack", "Espa\u00f1ol,Ingl\u00e9s,Portugu\u00e9s", "8", "Inmediata", "Disponible"},
                {"Diego", "Ram\u00edrez", "diego.ramirez@email.com", "Data Scientist", "Medell\u00edn", "Python,TensorFlow,PyTorch,SQL,Spark", "Espa\u00f1ol,Ingl\u00e9s", "6", "1 mes", "Seleccionado"},
                {"Sof\u00eda", "Torres", "sofia.torres@email.com", "Frontend Developer", "Bogot\u00e1", "React,Vue.js,TypeScript,Tailwind,Next.js", "Espa\u00f1ol,Ingl\u00e9s", "4", "Inmediata", "Disponible"},
                {"Andr\u00e9s", "Castro", "andres.castro@email.com", "Ingeniero de Sistemas", "Pereira", "Java,Python,SQL,Linux,Redes", "Espa\u00f1ol", "2", "2 semanas", "Registrado"},
            };

            for (String[] row : data) {
                if (usuarioRepository.findByEmail(row[2]).isPresent()) continue;

                Usuario u = new Usuario();
                u.setEmail(row[2]);
                u.setClave(passwordEncoder.encode("Test1234"));
                u.setRol("ROLE_CANDIDATO");
                u.setActivo(true);
                u = usuarioRepository.save(u);

                Candidato c = new Candidato();
                c.setId(u.getId());
                c.setUsername(row[0]);
                c.setApellido(row[1]);
                c.setCargo(row[3]);
                c.setCiudad(row[4]);
                c.setTecnologias(row[5]);
                c.setIdiomas(row[6]);
                c.setExperiencia(Integer.parseInt(row[7]));
                c.setDisponibilidad(row[8]);
                c.setEstado(row[9]);
                c.setUltimaActualizacion(LocalDateTime.now());
                candidatoRepository.save(c);

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
