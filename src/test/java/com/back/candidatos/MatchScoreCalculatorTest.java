package com.back.candidatos;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchScoreCalculatorTest {

    private Candidato candidatoBasico() {
        Candidato c = new Candidato();
        c.setUsername("Ana");
        return c;
    }

    @Test
    void calcularMatchScore_perfilVacioDaCero() {
        assertThat(MatchScoreCalculator.calcularMatchScore(candidatoBasico())).isZero();
    }

    @Test
    void calcularMatchScore_datosBasicosSumados() {
        Candidato c = candidatoBasico();
        c.setCargo("Desarrollador");
        c.setCiudad("Medellín");
        c.setTelefono("3001234567");

        assertThat(MatchScoreCalculator.calcularMatchScore(c)).isEqualTo(15);
    }

    @Test
    void calcularMatchScore_experienciaPonderaPorRango() {
        Candidato c1 = candidatoBasico();
        c1.setExperiencia(6);
        Candidato c2 = candidatoBasico();
        c2.setExperiencia(3);
        Candidato c3 = candidatoBasico();
        c3.setExperiencia(1);

        assertThat(MatchScoreCalculator.calcularMatchScore(c1)).isEqualTo(20);
        assertThat(MatchScoreCalculator.calcularMatchScore(c2)).isEqualTo(15);
        assertThat(MatchScoreCalculator.calcularMatchScore(c3)).isEqualTo(10);
    }

    @Test
    void calcularMatchScore_tecnologiasPonderanPorCantidad() {
        Candidato c5 = candidatoBasico();
        c5.setTecnologias("Java,Spring,SQL,Docker,AWS");
        Candidato c3 = candidatoBasico();
        c3.setTecnologias("Java,Spring,SQL");
        Candidato c1 = candidatoBasico();
        c1.setTecnologias("Java");

        assertThat(MatchScoreCalculator.calcularMatchScore(c5)).isEqualTo(30);
        assertThat(MatchScoreCalculator.calcularMatchScore(c3)).isEqualTo(20);
        assertThat(MatchScoreCalculator.calcularMatchScore(c1)).isEqualTo(15);
    }

    @Test
    void calcularMatchScore_perfilCompletoSuma95() {
        Candidato c = candidatoBasico();
        c.setCargo("DevOps");
        c.setCiudad("Bogotá");
        c.setTelefono("3000000000");
        c.setExperiencia(8);
        c.setTecnologias("Java,Spring,SQL,Docker,K8s,Azure,Git");
        c.setIdiomas("Español,Inglés");
        c.setDisponibilidad("Inmediata");

        assertThat(MatchScoreCalculator.calcularMatchScore(c)).isEqualTo(95);
    }

    @Test
    void getMatchLabel_segmentaLabels() {
        assertThat(MatchScoreCalculator.getMatchLabel(95)).isEqualTo("Excelente perfil");
        assertThat(MatchScoreCalculator.getMatchLabel(70)).isEqualTo("Buena coincidencia");
        assertThat(MatchScoreCalculator.getMatchLabel(50)).isEqualTo("Perfil en desarrollo");
        assertThat(MatchScoreCalculator.getMatchLabel(10)).isEqualTo("Perfil básico");
    }
}
