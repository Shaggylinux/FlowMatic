package com.back;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.auth.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TokenTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void testToken() {
        Usuario u = new Usuario();
        u.setEmail("test-reset@flowmatic.com");
        u.setClave("12345678");
        u.setRol("ROLE_CANDIDATO");
        usuarioRepository.save(u);

        usuarioService.generarTokenRecuperacion("test-reset@flowmatic.com");

        Usuario fromDb = usuarioRepository.findByEmail("test-reset@flowmatic.com").orElse(null);
        assertNotNull(fromDb);
        String token = fromDb.getTokenactivacion();
        assertNotNull(token);

        String estado = usuarioService.validarTokenReset(token);
        System.out.println("ESTADO_DEL_TOKEN: " + estado);
        
        assertEquals("VALIDA", estado);
    }
}
