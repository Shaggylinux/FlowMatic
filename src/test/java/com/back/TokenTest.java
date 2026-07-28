package com.back;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.auth.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.back.auth.Token;
import com.back.auth.TokenRepository;
import java.util.List;

@SpringBootTest
@Transactional
class TokenTest extends BaseIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    public void testToken() {
        tokenRepository.deleteAll();

        String email = "test-reset-" + System.currentTimeMillis() + "@flowmatic.com";
        Usuario u = new Usuario();
        u.setEmail(email);
        u.setClave("12345678");
        u.setRol("ROLE_CANDIDATO");
        u.setActivo(true);
        usuarioRepository.save(u);

        usuarioService.generarTokenRecuperacion(email);

        Usuario fromDb = usuarioRepository.findByEmail(email).orElse(null);
        assertNotNull(fromDb);
        
        List<Token> tokens = tokenRepository.findByUsuarioId(fromDb.getId());
        assertEquals(1, tokens.size());
        
        Token tokenObj = tokens.get(0);
        assertEquals("RESET_PASSWORD", tokenObj.getTipo());
        
        String tokenStr = tokenObj.getId();

        String estado = usuarioService.validarTokenReset(tokenStr);
        System.out.println("ESTADO_DEL_TOKEN: " + estado);
        
        assertEquals("VALIDA", estado);
    }
}
