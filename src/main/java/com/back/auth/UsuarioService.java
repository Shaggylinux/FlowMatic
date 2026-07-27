package com.back.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.back.admin.ConfiguracionService;
import com.back.admin.RRHH;
import com.back.admin.RRHHRepository;
import com.back.candidatos.Candidato;
import com.back.candidatos.CandidatoRepository;
import com.back.auth.event.UsuarioRegistradoEvent;
import com.back.drive.FilesServices;
import com.back.shared.EmailService;
import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;
    private final RRHHRepository rrhhRepository;
    private final EmailService emailService;
    private final ConfiguracionService configuracionService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String registrarUsuario(Usuario usuario, String username, String apellido, String telefono) {

        logger.info("Iniciando registro de usuario: {}", usuario.getEmail());

        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            logger.warn("Correo duplicado: {}", usuario.getEmail());
            return "DUPLICADO";
        }

        int minLength = Integer.parseInt(configuracionService.getValor("password.min.length", "8"));
        if (usuario.getClave() == null || usuario.getClave().trim().length() < minLength) {
            logger.warn("Contrase\u00f1a demasiado corta: {}", usuario.getEmail());
            return "CLAVE_CORTA";
        }

        usuario.setClave(encoder.encode(usuario.getClave()));

        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("ROLE_CANDIDATO");
        }

        String token = UUID.randomUUID().toString();
        usuario.setTokenactivacion(token);
        usuario.setActivo(false);
        usuario.setFechaCreacionToken(LocalDateTime.now());
        usuario = usuarioRepository.save(usuario);

        eventPublisher.publishEvent(new UsuarioRegistradoEvent(this, usuario.getId(),
            usuario.getEmail(), usuario.getRol(), username, apellido));

        if ("ROLE_RRHH".equals(usuario.getRol())) {
            RRHH rrhh = new RRHH();
            rrhh.setId(usuario.getId());
            rrhh.setUsername(username);
            rrhh.setApellido(apellido);
            if (telefono != null && !telefono.trim().isEmpty()) {
                rrhh.setTelefono(telefono);
            }
            rrhhRepository.save(rrhh);
        }

        logger.info("Intentando enviar email de verificaci\u00f3n a: {}", usuario.getEmail());

        boolean emailSent = emailService.enviarEmailVerificacion(
                usuario.getEmail(),
                username,
                token);

        if (emailSent) {
            logger.info("Email enviado correctamente");
        } else {
            logger.warn("Email no pudo ser enviado, pero el usuario se registr\u00f3");
        }

        return "EXITOSO";
    }

    public Usuario buscarPorToken(String token) {
        return usuarioRepository.findByTokenactivacion(token).orElse(null);
    }

    public void regenerarYReenviarToken(String tokenViejo) {
        Usuario usuario = usuarioRepository.findByTokenactivacion(tokenViejo).orElse(null);

        if (usuario != null) {
            String nuevoToken = UUID.randomUUID().toString();
            usuario.setTokenactivacion(nuevoToken);
            usuario.setFechaCreacionToken(LocalDateTime.now());
            usuarioRepository.save(usuario);

            String nombre = obtenerNombreOApellido(usuario);
            emailService.enviarEmailVerificacion(usuario.getEmail(), nombre, nuevoToken);
        }
    }

    public boolean activarCuenta(String token) {
        logger.info("Buscando token de activaci\u00f3n: {}", token);

        var optional = usuarioRepository.findByTokenactivacion(token);

        if (optional.isEmpty()) {
            logger.warn("Token no encontrado o inv\u00e1lido");
            return false;
        }

        Usuario usuario = optional.get();
        usuario.setActivo(true);
        usuario.setTokenactivacion(null);
        usuarioRepository.save(usuario);

        logger.info("Cuenta activada para: {}", usuario.getEmail());

        return true;
    }

    public void generarTokenRecuperacion(String email) {
        var optional = usuarioRepository.findByEmail(email);

        if (optional.isEmpty()) {
            return;
        }

        Usuario usuario = optional.get();

        String token = UUID.randomUUID().toString();
        usuario.setTokenactivacion(token);
        usuario.setFechaCreacionToken(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String nombre = obtenerNombreOApellido(usuario);
        emailService.enviarEmailRecuperacion(email, nombre, token);
    }

    public String validarTokenReset(String token) {
        var optional = usuarioRepository.findByTokenactivacion(token);
        if (optional.isEmpty()) return "INVALIDO";
        Usuario usuario = optional.get();
        if (usuario.getFechaCreacionToken() != null) {
            long minutos = java.time.Duration.between(usuario.getFechaCreacionToken(), LocalDateTime.now()).toMinutes();
            long expiry = Long.parseLong(configuracionService.getValor("password.reset.expiry.minutes", "15"));
            if (minutos > expiry) return "EXPIRADO";
        }
        return "VALIDA";
    }

    public boolean cambiarPassword(String token, String nuevaPassword) {
        var optional = usuarioRepository.findByTokenactivacion(token);

        if (optional.isEmpty()) {
            return false;
        }

        Usuario usuario = optional.get();

        if (usuario.getFechaCreacionToken() != null) {
            long minutos = java.time.Duration.between(usuario.getFechaCreacionToken(), LocalDateTime.now()).toMinutes();
            long expiry = Long.parseLong(configuracionService.getValor("password.reset.expiry.minutes", "15"));
            if (minutos > expiry) {
                usuario.setTokenactivacion(null);
                usuarioRepository.save(usuario);
                return false;
            }
        }

        usuario.setClave(encoder.encode(nuevaPassword));
        usuario.setTokenactivacion(null);

        usuarioRepository.save(usuario);

        return true;
    }

    private String obtenerNombreOApellido(Usuario usuario) {
        if ("ROLE_CANDIDATO".equals(usuario.getRol())) {
            return candidatoRepository.findById(usuario.getId())
                    .map(c -> c.getUsername() + " " + c.getApellido())
                    .orElse("Usuario");
        }
        if ("ROLE_RRHH".equals(usuario.getRol())) {
            return rrhhRepository.findById(usuario.getId())
                    .map(r -> r.getUsername() + " " + r.getApellido())
                    .orElse("RRHH");
        }
        return "Administrador";
    }
}
