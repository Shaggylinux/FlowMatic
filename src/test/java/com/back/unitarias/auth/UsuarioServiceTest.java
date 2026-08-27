package com.back.unitarias.auth;

import com.back.auth.*;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.shared.api.ConfiguracionApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private TokenRepository tokenRepository;
    @Mock
    private ConfiguracionApi configuracionService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, tokenRepository, configuracionService, eventPublisher);
    }

    private Token token(String uuid, Long usuarioId, String tipo) {
        return new Token(uuid, usuarioId, tipo, 86400L);
    }

    @Test
    void reenviarActivacion_porEmailNoRegistrado_devuelveNoRegistrado() {
        when(usuarioRepository.findByEmail("nadie@flowmatic.com")).thenReturn(Optional.empty());

        assertThat(usuarioService.reenviarActivacionPorEmail("nadie@flowmatic.com")).isEqualTo("NO_REGISTRADO");
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void reenviarActivacion_usuarioYaActivo_devuelveYaActiva() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("activo@flowmatic.com");
        u.setActivo(true);
        when(usuarioRepository.findByEmail("activo@flowmatic.com")).thenReturn(Optional.of(u));

        assertThat(usuarioService.reenviarActivacionPorEmail("activo@flowmatic.com")).isEqualTo("YA_ACTIVA");
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void reenviarActivacion_borraTokensPreviosYPublicaEventoConNuevoToken() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("pendiente@flowmatic.com");
        u.setActivo(false);
        when(usuarioRepository.findByEmail("pendiente@flowmatic.com")).thenReturn(Optional.of(u));
        Token viejo = token("token-viejo", 1L, "ACTIVACION");
        when(tokenRepository.findByUsuarioId(1L)).thenReturn(List.of(viejo));

        String resultado = usuarioService.reenviarActivacionPorEmail("pendiente@flowmatic.com");

        assertThat(resultado).isEqualTo("ENVIADO");
        verify(tokenRepository).delete(viejo);
        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("ACTIVACION");
        assertThat(captor.getValue().getId()).isNotEqualTo("token-viejo");
        verify(eventPublisher).publishEvent(any(UsuarioRegistradoEvent.class));
    }

    @Test
    void generarTokenRecuperacion_invalidaTokensResetPrevios() {
        Usuario u = new Usuario();
        u.setId(2L);
        u.setEmail("reset@flowmatic.com");
        when(usuarioRepository.findByEmail("reset@flowmatic.com")).thenReturn(Optional.of(u));
        Token previo = token("reset-viejo", 2L, "RESET_PASSWORD");
        when(tokenRepository.findByUsuarioId(2L)).thenReturn(List.of(previo));
        when(configuracionService.getValor(eq("password.reset.expiry.minutes"), anyString())).thenReturn("15");

        usuarioService.generarTokenRecuperacion("reset@flowmatic.com");

        verify(tokenRepository).delete(previo);
        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("RESET_PASSWORD");
        assertThat(captor.getValue().getTimeToLive()).isEqualTo(900L);
        verify(eventPublisher).publishEvent(any(PasswordResetSolicitadoEvent.class));
    }

    @Test
    void generarTokenRecuperacion_emailDesconocido_noPublicaEvento() {
        when(usuarioRepository.findByEmail("nadie@flowmatic.com")).thenReturn(Optional.empty());

        usuarioService.generarTokenRecuperacion("nadie@flowmatic.com");

        verify(tokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void cambiarPassword_tokenValido_actualizaClaveEliminaToken() {
        Usuario u = new Usuario();
        u.setId(3L);
        u.setEmail("clave@flowmatic.com");
        u.setClave("anterior");
        Token t = token("reset-activo", 3L, "RESET_PASSWORD");
        when(tokenRepository.findById("reset-activo")).thenReturn(Optional.of(t));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));

        boolean ok = usuarioService.cambiarPassword("reset-activo", "NuevaClave123");

        assertThat(ok).isTrue();
        assertThat(u.getClave()).isNotEqualTo("anterior");
        assertThat(u.getClave()).startsWith("$2");
        verify(usuarioRepository).save(u);
        verify(tokenRepository).delete(t);
    }

    @Test
    void cambiarPassword_tokenInvalido_devuelveFalse() {
        when(tokenRepository.findById("inexistente")).thenReturn(Optional.empty());

        assertThat(usuarioService.cambiarPassword("inexistente", "NuevaClave123")).isFalse();
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void activarCuenta_borraTokenYActivaUsuario() {
        Usuario u = new Usuario();
        u.setId(4L);
        u.setActivo(false);
        Token t = token("activo-ok", 4L, "ACTIVACION");
        when(tokenRepository.findById("activo-ok")).thenReturn(Optional.of(t));
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(u));

        assertThat(usuarioService.activarCuenta("activo-ok")).isTrue();
        assertThat(u.isActivo()).isTrue();
        verify(tokenRepository).delete(t);
    }

    @Test
    void buscarPorToken_soloAceptaTipoActivacion() {
        Token t = token("x", 5L, "RESET_PASSWORD");
        when(tokenRepository.findById("x")).thenReturn(Optional.of(t));

        assertThat(usuarioService.buscarPorToken("x")).isNull();
        verify(usuarioRepository, never()).findById(any());
    }
}
