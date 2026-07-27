package com.back.registro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String username;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo v\u00e1lido")
    private String email;

    @NotBlank(message = "La contrase\u00f1a es obligatoria")
    private String clave;

    private String cargo;
    private String ciudad;
    private String tecnologias;
    private String idiomas;
    private Integer experiencia;
    private String disponibilidad;
}
