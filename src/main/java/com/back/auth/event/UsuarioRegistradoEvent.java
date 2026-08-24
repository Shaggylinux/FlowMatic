package com.back.auth.event;

public record UsuarioRegistradoEvent(
    Long usuarioId,
    String email,
    String rol,
    String username,
    String apellido,
    String telefono,
    String documento,
    String cargo,
    String tokenActivacion,
    String rrhhEmail,
    String clavePlana
) {
    public UsuarioRegistradoEvent(
        Long usuarioId, String email, String rol, String username, String apellido,
        String telefono, String documento, String cargo, String tokenActivacion
    ) {
        this(usuarioId, email, rol, username, apellido, telefono, documento, cargo, tokenActivacion, null, null);
    }

    public UsuarioRegistradoEvent(
        Long usuarioId, String email, String rol, String username, String apellido,
        String telefono, String documento, String cargo, String tokenActivacion, String rrhhEmail
    ) {
        this(usuarioId, email, rol, username, apellido, telefono, documento, cargo, tokenActivacion, rrhhEmail, null);
    }
}
