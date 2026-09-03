package com.back.unitarias.candidatos;

import com.back.candidatos.Candidato;
import com.back.candidatos.MatchScoreCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EjemploCalculoMatchScoreTest {

    @Test
    @DisplayName("Candidato Senior con perfil completo debe obtener puntuación alta (>= 80%)")
    void candidatoSeniorDebeObtenerPuntajeAlto() {
        // 1. ARRANGE (Preparación de datos del candidato)
        Candidato candidato = new Candidato();
        candidato.setUsername("Ana");
        candidato.setApellido("Gómez");
        candidato.setCargo("Desarrollador Senior");
        candidato.setCiudad("Medellín");
        candidato.setTelefono("3001234567");
        candidato.setExperiencia(6); // 6 años de experiencia (+20 pts)
        candidato.setTecnologias("Java,Spring,PostgreSQL,Docker,AWS"); // 5 tecnologías (+30 pts)
        candidato.setIdiomas("Español,Inglés"); // 2 idiomas (+15 pts)
        candidato.setDisponibilidad("Inmediata"); // Disponibilidad inmediata (+15 pts)

        // 2. ACT (Ejecución del cálculo matemático y etiqueta)
        int scoreObtenido = MatchScoreCalculator.calcularMatchScore(candidato);
        String etiqueta = MatchScoreCalculator.getMatchLabel(scoreObtenido);

        // 3. ASSERT (Comprobaciones y afirmaciones lógicas)
        System.out.println("✅ [LOG PRUEBA] Match Score para Ana Gómez: " + scoreObtenido + "% (" + etiqueta + ")");
        assertEquals(95, scoreObtenido, "Un perfil completo debe dar exactamente 95%");
        assertEquals("Excelente perfil", etiqueta, "Con score >= 80 la etiqueta debe ser 'Excelente perfil'");
    }

    @Test
    @DisplayName("Candidato sin experiencia ni tecnologías debe obtener exactamente 0%")
    void candidatoVacioDebeObtenerCero() {
        // 1. ARRANGE (Candidato sin datos)
        Candidato candidatoVacio = new Candidato();
        candidatoVacio.setUsername("nuevo@flowmatic.com");

        // 2. ACT (Ejecución del cálculo)
        int scoreObtenido = MatchScoreCalculator.calcularMatchScore(candidatoVacio);

        // 3. ASSERT (Comprobación de resultado 0)
        System.out.println("✅ [LOG PRUEBA] Match Score para perfil vacío: " + scoreObtenido + "%");
        assertEquals(0, scoreObtenido, "Un perfil vacío debe dar 0% de coincidencia");
    }
}
