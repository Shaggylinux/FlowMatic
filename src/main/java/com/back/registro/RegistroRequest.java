package com.back.registro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$", message = "El nombre solo puede contener letras y espacios")
    private String username;

    @NotBlank(message = "El apellido es obligatorio")
    @Pattern(regexp = "^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$", message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Ingresa un correo válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String clave;

    private String confirmarClave;

    private String cargo;
    private String ciudad;
    private String tecnologias;
    private String idiomas;
    private Integer experiencia;
    private String disponibilidad;
}
