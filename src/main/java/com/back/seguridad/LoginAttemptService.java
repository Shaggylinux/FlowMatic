package com.back.seguridad;

import com.back.admin.ConfiguracionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    @Autowired
    private ConfiguracionService configuracionService;

    private final ConcurrentHashMap<String, LoginAttemptInfo> cache = new ConcurrentHashMap<>();

    public void recordFailed(String email) {
        int maxAttempts = Integer.parseInt(configuracionService.getValor("login.max.attempts", "5"));
        long blockMinutes = Long.parseLong(configuracionService.getValor("login.block.minutes", "15"));
        cache.merge(
            email.toLowerCase(),
            LoginAttemptInfo.first(),
            (old, val) -> old.isBlocked() ? old : old.increment(maxAttempts, blockMinutes)
        );
    }

    public boolean isBlocked(String email) {
        LoginAttemptInfo info = cache.get(email.toLowerCase());
        return info != null && info.isBlocked();
    }

    public void reset(String email) {
        cache.remove(email.toLowerCase());
    }
}
