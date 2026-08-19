package com.back.admin;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.back.admin.dto.UsuarioRRHHDTO;
import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.shared.event.RRHHEliminadoEvent;

@ExtendWith(MockitoExtension.class)
class AdminValidacionesTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RRHHRepository rrhhRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditoriaService auditoriaService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Principal principal;

    private AdminRRHHRestController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminRRHHRestController(
            usuarioRepository,
            rrhhRepository,
            passwordEncoder,
            auditoriaService,
            eventPublisher
        );
    }

    @Test
    @DisplayName("Actualizar RRHH con documento mayor a 10 dígitos debe retornar BAD_REQUEST")
    void updateRRHH_documentoInvalido_retornaBadRequest() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("ana@empresa.com");
        u.setRol("ROLE_RRHH");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        UsuarioRRHHDTO dto = new UsuarioRRHHDTO();
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setEmail("ana@empresa.com");
        dto.setDocumento("123456789012"); // 12 dígitos > 10
        dto.setTelefono("3001234567");

        ResponseEntity<?> response = controller.updateUsuarioRRHH(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(rrhhRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar RRHH con teléfono distinto a 10 dígitos debe retornar BAD_REQUEST")
    void updateRRHH_telefonoInvalido_retornaBadRequest() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("ana@empresa.com");
        u.setRol("ROLE_RRHH");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));

        UsuarioRRHHDTO dto = new UsuarioRRHHDTO();
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setEmail("ana@empresa.com");
        dto.setDocumento("1234567890"); // 10 dígitos OK
        dto.setTelefono("300123");     // 6 dígitos < 10

        ResponseEntity<?> response = controller.updateUsuarioRRHH(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(rrhhRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar RRHH con documento y teléfono de 10 dígitos válidos debe ser exitoso")
    void updateRRHH_datosValidos_retornaOk() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("carlos@empresa.com");
        u.setRol("ROLE_RRHH");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(rrhhRepository.findById(1L)).thenReturn(Optional.of(new RRHH()));

        UsuarioRRHHDTO dto = new UsuarioRRHHDTO();
        dto.setNombre("Carlos");
        dto.setApellido("Gomez");
        dto.setEmail("carlos@empresa.com");
        dto.setDocumento("1020304050");
        dto.setTelefono("3109876543");
        dto.setCargo("Reclutador Senior");

        ResponseEntity<?> response = controller.updateUsuarioRRHH(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(usuarioRepository).save(any());
        verify(rrhhRepository).save(any());
    }

    @Test
    @DisplayName("Eliminar RRHH debe publicar RRHHEliminadoEvent y eliminar entidades")
    void deleteUsuario_publicaEventoYElimina() {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setEmail("eliminar@empresa.com");
        u.setRol("ROLE_RRHH");

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(u));
        when(principal.getName()).thenReturn("AdminUser");

        ResponseEntity<?> response = controller.deleteUsuario(5L, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(eventPublisher).publishEvent(any(RRHHEliminadoEvent.class));
        verify(rrhhRepository).deleteById(5L);
        verify(usuarioRepository).delete(u);
    }
}
