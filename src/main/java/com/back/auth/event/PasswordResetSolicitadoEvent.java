package com.back.auth.event;

public record PasswordResetSolicitadoEvent(
    String email,
    String nombre,
    String tokenReset
) {}
