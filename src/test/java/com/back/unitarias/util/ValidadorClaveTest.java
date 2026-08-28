package com.back.unitarias.util;

import com.back.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorClaveTest {

    @Test
    @DisplayName("Debe aceptar contraseña segura con mayúscula, minúscula, número y carácter especial")
    void debeAceptarClaveSegura() {
        assertTrue(ValidadorClave.esClaveSegura("Flowmatic2026!"));
        assertTrue(ValidadorClave.esClaveSegura("Pass123#"));
        assertTrue(ValidadorClave.esClaveSegura("A1b2c3d4$"));
    }

    @Test
    @DisplayName("Debe rechazar contraseñas de menos de 8 caracteres")
    void debeRechazarClaveCorta() {
        assertFalse(ValidadorClave.esClaveSegura("Pass1!")); // 6 caracteres
        assertFalse(ValidadorClave.esClaveSegura("P1!a2#b")); // 7 caracteres
    }

    @Test
    @DisplayName("Debe rechazar contraseñas sin mayúsculas")
    void debeRechazarSinMayuscula() {
        assertFalse(ValidadorClave.esClaveSegura("flowmatic2026!"));
    }

    @Test
    @DisplayName("Debe rechazar contraseñas sin minúsculas")
    void debeRechazarSinMinuscula() {
        assertFalse(ValidadorClave.esClaveSegura("FLOWMATIC2026!"));
    }

    @Test
    @DisplayName("Debe rechazar contraseñas sin números")
    void debeRechazarSinNumero() {
        assertFalse(ValidadorClave.esClaveSegura("FlowmaticSecure!"));
    }

    @Test
    @DisplayName("Debe rechazar contraseñas sin carácter especial")
    void debeRechazarSinEspecial() {
        assertFalse(ValidadorClave.esClaveSegura("Flowmatic2026"));
    }

    @Test
    @DisplayName("Debe rechazar nulos o vacíos")
    void debeRechazarNuloOVacio() {
        assertFalse(ValidadorClave.esClaveSegura(null));
        assertFalse(ValidadorClave.esClaveSegura("   "));
    }
}
