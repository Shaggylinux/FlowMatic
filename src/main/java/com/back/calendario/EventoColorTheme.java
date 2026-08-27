package com.back.calendario;

public record EventoColorTheme(String bg, String border, String text) {

    public static final EventoColorTheme CONFIRMADO = new EventoColorTheme("#DCFCE7", "#22C55E", "#166534");
    public static final EventoColorTheme REPROGRAMADO = new EventoColorTheme("#FFEDD5", "#F97316", "#9A3412");
    public static final EventoColorTheme CANCELADO = new EventoColorTheme("#FEE2E2", "#EF4444", "#991B1B");
    public static final EventoColorTheme REALIZADA = new EventoColorTheme("#F1F5F9", "#94A3B8", "#475569");
    public static final EventoColorTheme DEFAULT = new EventoColorTheme("#DBEAFE", "#2563EB", "#1D4ED8");

    public static EventoColorTheme of(String estado) {
        if (estado == null) {
            return DEFAULT;
        }
        return switch (estado) {
            case "CONFIRMADO" -> CONFIRMADO;
            case "REPROGRAMADO" -> REPROGRAMADO;
            case "CANCELADO" -> CANCELADO;
            case "REALIZADA" -> REALIZADA;
            default -> DEFAULT;
        };
    }
}
