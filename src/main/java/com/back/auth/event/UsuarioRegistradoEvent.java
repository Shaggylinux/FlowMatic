package com.back.auth.event;

public record UsuarioRegistradoEvent(
    Long usuarioId,
    String email,
    String rol,
    String username,
    String apellido,
    String telefono,
    String tokenActivacion
) {}
