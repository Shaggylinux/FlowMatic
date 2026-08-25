package com.back.auth;


import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.back.auth.event.PasswordResetSolicitadoEvent;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.shared.api.AuthApi;
import com.back.shared.api.ConfiguracionApi;
import com.back.shared.dto.RegistroUsuarioDTO;
import com.back.shared.exception.ClaveCortaException;
import com.back.shared.exception.UsuarioDuplicadoException;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService implements AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final TokenRepository tokenRepository;

    private final ConfiguracionApi configuracionService;
    private final ApplicationEventPublisher eventPublisher;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void registrarUsuario(RegistroUsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setClave(dto.getClave());
        usuario.setRol(dto.getRol());
        
        String username = dto.getUsername();
        String apellido = dto.getApellido();
        String telefono = dto.getTelefono();

        if (username == null || username.isBlank() || !username.trim().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios (de 2 a 50 caracteres)");
        }

        if (apellido == null || apellido.isBlank() || !apellido.trim().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
            throw new IllegalArgumentException("El apellido solo puede contener letras y espacios (de 2 a 50 caracteres)");
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            logger.warn("Correo duplicado: {}", usuario.getEmail());
            throw new UsuarioDuplicadoException("El email ya está registrado");
        }

        if (!com.back.util.ValidadorClave.esClaveSegura(usuario.getClave())) {
            logger.warn("Contraseña no cumple requisitos de complejidad: {}", usuario.getEmail());
            throw new ClaveCortaException("La contraseña debe tener mínimo 8 caracteres, mayúsculas, minúsculas, un número y un carácter especial");
        }

        String clavePlana = dto.getClave();
        usuario.setClave(encoder.encode(usuario.getClave()));

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("ROLE_CANDIDATO");
        }

        usuario.setActivo(false);
        usuario = usuarioRepository.save(usuario);

        String tokenUuid = UUID.randomUUID().toString();
        Token tokenObj = new Token(tokenUuid, usuario.getId(), "ACTIVACION", 86400L); // 24 horas
        tokenRepository.save(tokenObj);

        eventPublisher.publishEvent(new UsuarioRegistradoEvent(usuario.getId(),
            usuario.getEmail(), usuario.getRol(), username, apellido, telefono, dto.getDocumento(), dto.getCargo(), tokenUuid, dto.getRrhhEmail(), clavePlana));
    }

    public Usuario buscarPorToken(String token) {
        Token t = tokenRepository.findById(token).orElse(null);
        if (t != null && "ACTIVACION".equals(t.getTipo())) {
            return usuarioRepository.findById(t.getUsuarioId()).orElse(null);
        }
        return null;
    }

    public Usuario buscarPorTokenReset(String token) {
        Token t = tokenRepository.findById(token).orElse(null);
        if (t != null && "RESET_PASSWORD".equals(t.getTipo())) {
            return usuarioRepository.findById(t.getUsuarioId()).orElse(null);
        }
        return null;
    }

    @org.springframework.transaction.annotation.Transactional
    public String reenviarActivacionPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return "NO_REGISTRADO";
        }

        var optional = usuarioRepository.findByEmail(email.trim());
        if (optional.isEmpty()) {
            return "NO_REGISTRADO";
        }

        Usuario usuario = optional.get();
        if (usuario.isActivo()) {
            return "YA_ACTIVA";
        }

        tokenRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(t -> "ACTIVACION".equals(t.getTipo()))
                .forEach(tokenRepository::delete);

        String nuevoToken = UUID.randomUUID().toString();
        Token newTokenObj = new Token(nuevoToken, usuario.getId(), "ACTIVACION", 86400L);
        tokenRepository.save(newTokenObj);

        String nombre = obtenerNombreOApellido(usuario);
        eventPublisher.publishEvent(new UsuarioRegistradoEvent(usuario.getId(),
            usuario.getEmail(), usuario.getRol(), nombre, null, null, null, null, nuevoToken));

        return "ENVIADO";
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean activarCuenta(String token) {
        logger.info("Buscando token de activación: {}", token);

        Token t = tokenRepository.findById(token).orElse(null);
        if (t == null || !"ACTIVACION".equals(t.getTipo())) {
            logger.warn("Token no encontrado o inválido: {}", token);
            return false;
        }

        Usuario usuario = usuarioRepository.findById(t.getUsuarioId()).orElse(null);
        if (usuario != null) {
            usuario.setActivo(true);
            usuarioRepository.saveAndFlush(usuario);
            tokenRepository.delete(t);
            logger.info("Cuenta activada exitosamente para: {}", usuario.getEmail());
            return true;
        }
        return false;
    }

    @org.springframework.transaction.annotation.Transactional
    public void generarTokenRecuperacion(String email) {
        var optional = usuarioRepository.findByEmail(email);

        if (optional.isEmpty()) {
            return;
        }

        Usuario usuario = optional.get();

        tokenRepository.findByUsuarioId(usuario.getId()).stream()
                .filter(t -> "RESET_PASSWORD".equals(t.getTipo()))
                .forEach(tokenRepository::delete);

        String tokenUuid = UUID.randomUUID().toString();
        long expiryMinutes = Long.parseLong(configuracionService.getValor("password.reset.expiry.minutes", "15"));
        Token tokenObj = new Token(tokenUuid, usuario.getId(), "RESET_PASSWORD", expiryMinutes * 60);
        tokenRepository.save(tokenObj);

        String nombre = obtenerNombreOApellido(usuario);
        eventPublisher.publishEvent(new PasswordResetSolicitadoEvent(email, nombre, tokenUuid));
    }

    public String validarTokenReset(String token) {
        Token t = tokenRepository.findById(token).orElse(null);
        if (t == null || !"RESET_PASSWORD".equals(t.getTipo())) {
            return "INVALIDO";
        }
        // Si el token existe en Redis, es v\u00e1lido (expira autom\u00e1ticamente por TTL)
        return "VALIDA";
    }

    public boolean cambiarPassword(String token, String nuevaPassword) {
        Token t = tokenRepository.findById(token).orElse(null);
        if (t == null || !"RESET_PASSWORD".equals(t.getTipo())) {
            return false;
        }

        Usuario usuario = usuarioRepository.findById(t.getUsuarioId()).orElse(null);
        if (usuario != null) {
            usuario.setClave(encoder.encode(nuevaPassword));
            usuarioRepository.save(usuario);
            tokenRepository.delete(t);
            return true;
        }
        
        return false;
    }

    public java.util.Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public java.util.Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean existePorEmail(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    public java.util.List<Usuario> buscarTodosPorIds(Iterable<Long> ids) {
        return usuarioRepository.findAllById(ids);
    }

    public java.util.Map<Long, Usuario> mapearUsuariosPorIds(Iterable<Long> ids) {
        return usuarioRepository.findAllById(ids).stream()
                .collect(java.util.stream.Collectors.toMap(Usuario::getId, u -> u));
    }

    @org.springframework.transaction.annotation.Transactional
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @org.springframework.transaction.annotation.Transactional
    public void eliminar(Usuario usuario) {
        usuarioRepository.delete(usuario);
    }

    @org.springframework.transaction.annotation.Transactional
    public void eliminarPorId(Long id) {
        usuarioRepository.deleteById(id);
    }

    public long contar() {
        return usuarioRepository.count();
    }

    public long contarPorRol(String rol) {
        return usuarioRepository.countByRol(rol);
    }

    public long contarPorRolYBloqueado(String rol, boolean bloqueado) {
        return usuarioRepository.countByRolAndBloqueado(rol, bloqueado);
    }

    public long contarPorRolYActivoYBloqueado(String rol, boolean activo, boolean bloqueado) {
        return usuarioRepository.countByRolAndActivoAndBloqueado(rol, activo, bloqueado);
    }

    public long contarPorRolYActivo(String rol, boolean activo) {
        return usuarioRepository.countByRolAndActivo(rol, activo);
    }

    public org.springframework.data.domain.Page<Usuario> buscarRRHH(String buscar, String estado, org.springframework.data.domain.Pageable pageable) {
        return usuarioRepository.buscarRRHH(buscar, estado, pageable);
    }

    public java.util.List<Usuario> buscarRRHHSinPaginacion(String buscar, String estado) {
        return usuarioRepository.buscarRRHHSinPaginacion(buscar, estado);
    }

    public java.util.List<Usuario> buscarTodos() {
        return usuarioRepository.findAll();
    }

    private String obtenerNombreOApellido(Usuario usuario) {
        String email = usuario.getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Usuario";
    }
}
