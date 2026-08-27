package com.back.unitarias.util;

import com.back.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SanitizerTest {

    @Test
    @DisplayName("Debe sanitizar rutas eliminando secuencias de Path Traversal (..)")
    void debeSanitizarPathTraversal() {
        assertEquals("etc/passwd", Sanitizer.sanitizePath("../../etc/passwd"));
        assertEquals("superfolder/docs", Sanitizer.sanitizePath("superfolder/../docs/"));
    }

    @Test
    @DisplayName("Debe normalizar diagonales invertidas a diagonales normales")
    void debeNormalizarDiagonales() {
        assertEquals("carpeta/archivo.pdf", Sanitizer.sanitizePath("carpeta\\archivo.pdf"));
    }

    @Test
    @DisplayName("Debe manejar cadenas nulas o vacías retornando cadena vacía")
    void debeManejarNulosYVacios() {
        assertEquals("", Sanitizer.sanitizePath(null));
        assertEquals("", Sanitizer.sanitizePath("   "));
    }

    @Test
    @DisplayName("Debe validar nombres de archivo seguros sin diagonales ni secuencias ..")
    void debeValidarNombresSeguros() {
        assertTrue(Sanitizer.isValidFileName("hoja_de_vida.pdf"));
        assertTrue(Sanitizer.isValidFileName("certificado-laboral-2026.docx"));

        assertFalse(Sanitizer.isValidFileName("../hack.sh"));
        assertFalse(Sanitizer.isValidFileName("carpeta/archivo.pdf"));
        assertFalse(Sanitizer.isValidFileName("carpeta\\archivo.pdf"));
        assertFalse(Sanitizer.isValidFileName(null));
        assertFalse(Sanitizer.isValidFileName("   "));
    }
}
