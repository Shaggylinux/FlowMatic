package com.back.candidatos;

public class MatchScoreCalculator {

    private MatchScoreCalculator() {
        // Utility class
    }

    public static int calcularMatchScore(Candidato candidato) {
        int score = 0;

        if (candidato.getCargo() != null && !candidato.getCargo().isBlank()) score += 5;
        if (candidato.getCiudad() != null && !candidato.getCiudad().isBlank()) score += 5;
        if (candidato.getTelefono() != null && !candidato.getTelefono().isBlank()) score += 5;

        if (candidato.getExperiencia() != null && candidato.getExperiencia() > 0) {
            if (candidato.getExperiencia() >= 5) score += 20;
            else if (candidato.getExperiencia() >= 2) score += 15;
            else score += 10;
        }

        if (candidato.getTecnologias() != null && !candidato.getTecnologias().isBlank()) {
            String[] tecs = candidato.getTecnologias().split(",");
            if (tecs.length >= 5) score += 30;
            else if (tecs.length >= 3) score += 20;
            else score += 15;
        }

        if (candidato.getIdiomas() != null && !candidato.getIdiomas().isBlank()) {
            String[] langs = candidato.getIdiomas().split(",");
            if (langs.length >= 2) score += 15;
            else score += 10;
        }

        if (candidato.getDisponibilidad() != null && !candidato.getDisponibilidad().isBlank()) {
            String disp = candidato.getDisponibilidad().toLowerCase();
            if (disp.contains("inmediata")) score += 15;
            else if (disp.contains("semana") || disp.contains("día")) score += 10;
            else score += 5;
        }

        return Math.min(score, 100);
    }

    public static String getMatchLabel(int score) {
        if (score >= 80) return "Excelente perfil";
        if (score >= 60) return "Buena coincidencia";
        if (score >= 40) return "Perfil en desarrollo";
        return "Perfil básico";
    }
}
