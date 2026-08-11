package com.back.seguridad;

import com.back.shared.api.ConfiguracionApi;
import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.shared.event.CuentaBloqueadaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private final ConfiguracionApi configuracionService;

    private final LoginAttemptRepository loginAttemptRepository;

    private final UsuarioRepository usuarioRepository;

    private final ApplicationEventPublisher eventPublisher;

    public LoginAttemptService(ConfiguracionApi configuracionService, LoginAttemptRepository loginAttemptRepository,
                               UsuarioRepository usuarioRepository, ApplicationEventPublisher eventPublisher) {
        this.configuracionService = configuracionService;
        this.loginAttemptRepository = loginAttemptRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void recordFailed(String email) {
        String normalizedEmail = email.toLowerCase();
        int maxAttempts = Integer.parseInt(configuracionService.getValor("login.max.attempts", "5"));
        long blockMinutes = Long.parseLong(configuracionService.getValor("login.block.minutes", "15"));
        
        LoginAttempt attempt = loginAttemptRepository.findByEmail(normalizedEmail)
            .orElse(new LoginAttempt(normalizedEmail));
            
        if (attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(LocalDateTime.now())) {
            return; // Already blocked
        }
        
        attempt.setAttempts(attempt.getAttempts() + 1);
        
        if (attempt.getAttempts() >= maxAttempts) {
            attempt.setBlockedUntil(LocalDateTime.now().plusMinutes(blockMinutes));
            attempt.setAttempts(0); // Optional: reset attempts once blocked
        }
        
        loginAttemptRepository.save(attempt);
    }

    public boolean isBlocked(String email) {
        return loginAttemptRepository.findByEmail(email.toLowerCase())
            .map(attempt -> attempt.getBlockedUntil() != null && attempt.getBlockedUntil().isAfter(LocalDateTime.now()))
            .orElse(false);
    }

    @Transactional
    public void reset(String email) {
        loginAttemptRepository.deleteByEmail(email.toLowerCase());
    }

    @Transactional
    public void publicarEventoBloqueo(String email) {
        long blockMinutes = Long.parseLong(configuracionService.getValor("login.block.minutes", "15"));
        String nombre = "Usuario";
        java.util.Optional<Usuario> uOpt = usuarioRepository.findByEmail(email.toLowerCase());
        if (uOpt.isPresent()) {
            Usuario u = uOpt.get();
            String correo = u.getEmail();
            if (correo != null && correo.contains("@")) {
                nombre = correo.substring(0, correo.indexOf("@"));
            }
        }
        eventPublisher.publishEvent(new CuentaBloqueadaEvent(email, nombre, blockMinutes));
    }
}
