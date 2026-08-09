package com.back.bootstrap;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TestCandidatesSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (candidatoRepository.count() > 0) {
            return;
        }

        String[][] candidatesData = {
                {"candidato1@demo.com", "Juan", "Pérez", "Bogotá", "Developer", "Registrado", "3", "Java, Spring, SQL"},
                {"candidato2@demo.com", "María", "Gómez", "Medellín", "Diseñador UX", "En pruebas", "5", "Figma, Adobe XD"},
                {"candidato3@demo.com", "Carlos", "López", "Cali", "QA Automation", "Entrevista", "2", "Selenium, Cypress"},
                {"candidato4@demo.com", "Ana", "Martínez", "Bogotá", "Developer", "Contratado", "7", "React, Node.js"},
                {"candidato5@demo.com", "Luis", "Hernández", "Barranquilla", "Data Analyst", "No aceptado", "1", "Python, Pandas, SQL"},
                {"candidato6@demo.com", "Sofía", "Ramírez", "Bucaramanga", "Project Manager", "Entrevista", "10", "Scrum, Jira, Agile"},
                {"candidato7@demo.com", "Diego", "Torres", "Pereira", "DevOps Engineer", "Registrado", "4", "Docker, Kubernetes, AWS"},
                {"candidato8@demo.com", "Valentina", "Díaz", "Bogotá", "Marketing", "En pruebas", "3", "SEO, SEM, Google Analytics"},
                {"candidato9@demo.com", "Andrés", "Castro", "Medellín", "Developer", "Registrado", "0", "HTML, CSS, JS"},
                {"candidato10@demo.com", "Camila", "Rojas", "Cali", "Diseñador Gráfico", "Contratado", "6", "Photoshop, Illustrator"}
        };

        for (String[] data : candidatesData) {
            Usuario u = new Usuario();
            u.setEmail(data[0]);
            u.setClave(passwordEncoder.encode("123456"));
            u.setRol("ROLE_CANDIDATO");
            u.setActivo(true);
            usuarioRepository.save(u);

            Candidato c = new Candidato();
            c.setId(u.getId());
            c.setUsername(data[1]);
            c.setApellido(data[2]);
            c.setCiudad(data[3]);
            c.setCargo(data[4]);
            c.setEstado(data[5]);
            c.setExperiencia(Integer.parseInt(data[6]));
            c.setTecnologias(data[7]);
            c.setTelefono("3000000000");
            c.setDisponibilidad("Inmediata");
            c.setIdiomas("Inglés B1");
            c.setProcesoActual("Revisión CV");
            c.setUltimaActualizacion(LocalDateTime.now().minusDays((long) (Math.random() * 10)));
            candidatoRepository.save(c);
        }

        System.out.println("✅ 10 Candidatos de prueba insertados exitosamente.");
    }
}
