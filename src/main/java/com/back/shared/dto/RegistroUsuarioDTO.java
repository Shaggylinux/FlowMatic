package com.back.shared.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistroUsuarioDTO {
    private String username;
    private String apellido;
    private String email;
    private String clave;
    private String telefono;
    private String rol;
    private String documento;
    private String cargo;
    private String rrhhEmail;
}
