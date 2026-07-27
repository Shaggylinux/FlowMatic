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
@Transactional
class TokenTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void testToken() {
        String email = "test-reset-" + System.currentTimeMillis() + "@flowmatic.com";
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setClave("12345678");
        u.setRol("ROLE_CANDIDATO");
        usuarioRepository.save(u);

        usuarioService.generarTokenRecuperacion(email);

        Usuario fromDb = usuarioRepository.findByEmail(email).orElse(null);
        assertNotNull(fromDb);
        String token = fromDb.getTokenResetPassword();
        assertNotNull(token);

        String estado = usuarioService.validarTokenReset(token);
        System.out.println("ESTADO_DEL_TOKEN: " + estado);
        
        assertEquals("VALIDA", estado);
    }
}
