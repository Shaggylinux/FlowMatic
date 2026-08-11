package com.back.util;

public class ValidadorClave {

    private ValidadorClave() {}

    /**
     * Valida que la contraseña cumpla los requisitos de seguridad:
     * - Mínimo 8 caracteres
     * - Al menos una letra mayúscula (A-Z)
     * - Al menos una letra minúscula (a-z)
     * - Al menos un número (0-9)
     * - Al menos un carácter especial (ej: !@#$%^&*()_+-=[]{};':"|,.<>/?~)
     */
    public static boolean esClaveSegura(String clave) {
        if (clave == null) return false;
        String trimmed = clave.trim();
        if (trimmed.length() < 8) return false;

        boolean tieneMayuscula = trimmed.chars().anyMatch(Character::isUpperCase);
        boolean tieneMinuscula = trimmed.chars().anyMatch(Character::isLowerCase);
        boolean tieneNumero = trimmed.chars().anyMatch(Character::isDigit);
        boolean tieneEspecial = trimmed.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial;
    }
}
