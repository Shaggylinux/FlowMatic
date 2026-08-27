package com.back.unitarias.seguridad;

import com.back.seguridad.*;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.shared.api.ConfiguracionApi;
import com.back.shared.event.CuentaBloqueadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private ConfiguracionApi configuracionService;
    @Mock
    private LoginAttemptRepository loginAttemptRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(configuracionService, loginAttemptRepository, usuarioRepository, eventPublisher);
        org.mockito.Mockito.lenient().when(configuracionService.getValor(eq("login.max.attempts"), anyString())).thenReturn("5");
        org.mockito.Mockito.lenient().when(configuracionService.getValor(eq("login.block.minutes"), anyString())).thenReturn("15");
    }

    @Test
    void recordFailed_alQuintoIntentoBloqueaCuenta() {
        LoginAttempt attempt = new LoginAttempt("user@flowmatic.com");
        when(loginAttemptRepository.findByEmail("user@flowmatic.com")).thenAnswer(inv -> Optional.of(attempt));

        for (int i = 1; i <= 5; i++) {
            service.recordFailed("User@Flowmatic.com");
        }

        ArgumentCaptor<LoginAttempt> captor = org.mockito.ArgumentCaptor.forClass(LoginAttempt.class);
        verify(loginAttemptRepository, times(5)).save(captor.capture());
        LoginAttempt last = captor.getValue();
        assertThat(last.getAttempts()).isZero();
        assertThat(last.getBlockedUntil()).isNotNull();
    }

    @Test
    void recordFailed_cuentaYaBloqueadaNoIncrementa() {
        LoginAttempt attempt = new LoginAttempt("user@flowmatic.com");
        attempt.setBlockedUntil(java.time.LocalDateTime.now().plusMinutes(10));
        when(loginAttemptRepository.findByEmail("user@flowmatic.com")).thenReturn(Optional.of(attempt));

        service.recordFailed("user@flowmatic.com");

        verify(loginAttemptRepository, never()).save(any());
    }

    @Test
    void isBlocked_devuelveTrueSiVigente() {
        LoginAttempt attempt = new LoginAttempt("user@flowmatic.com");
        attempt.setBlockedUntil(java.time.LocalDateTime.now().plusMinutes(10));
        when(loginAttemptRepository.findByEmail("user@flowmatic.com")).thenReturn(Optional.of(attempt));

        assertThat(service.isBlocked("USER@flowmatic.com")).isTrue();
    }

    @Test
    void isBlocked_devuelveFalseSiExpirado() {
        LoginAttempt attempt = new LoginAttempt("user@flowmatic.com");
        attempt.setBlockedUntil(java.time.LocalDateTime.now().minusMinutes(1));
        when(loginAttemptRepository.findByEmail("user@flowmatic.com")).thenReturn(Optional.of(attempt));

        assertThat(service.isBlocked("user@flowmatic.com")).isFalse();
    }

    @Test
    void reset_eliminaIntentos() {
        service.reset("User@Flowmatic.com");
        verify(loginAttemptRepository).deleteByEmail("user@flowmatic.com");
    }

    @Test
    void publicarEventoBloqueo_usaNombreDelEmailYMinutosConfig() {
        Usuario u = new Usuario();
        u.setEmail("carlos@flowmatic.com");
        when(usuarioRepository.findByEmail("carlos@flowmatic.com")).thenReturn(Optional.of(u));

        service.publicarEventoBloqueo("carlos@flowmatic.com");

        verify(eventPublisher).publishEvent(any(CuentaBloqueadaEvent.class));
    }

    @Test
    void publicarEventoBloqueo_sinUsuarioUsaNombreGenerico() {
        when(usuarioRepository.findByEmail("anon@flowmatic.com")).thenReturn(Optional.empty());

        service.publicarEventoBloqueo("anon@flowmatic.com");

        verify(eventPublisher).publishEvent(any(CuentaBloqueadaEvent.class));
    }
}
