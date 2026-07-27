package com.back.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadRecienteDTO {
    private String titulo;
    private String usuario;
    private String fecha;
    private String tipo;
    private String iniciales;
    private String colorAvatar;
}
