package com.back.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResumenDTO {
    private Long id;
    private String username;
    private String apellido;
    private String email;
    private String rol;
    private boolean activo;
    private String fechaRegistro;
}
