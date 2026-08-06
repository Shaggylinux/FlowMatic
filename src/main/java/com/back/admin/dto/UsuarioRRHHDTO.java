package com.back.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRRHHDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String documento;
    private String cargo;
    private String rol;
    private String estado;
    private boolean activo;
    private LocalDateTime ultimoAcceso;
}
