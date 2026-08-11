package com.back.shared.event;

public record CuentaBloqueadaEvent(
    String email,
    String nombre,
    long minutos
) {
}
