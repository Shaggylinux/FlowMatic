package com.back.shared;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final ConcurrentHashMap<String, LoginAttemptInfo> cache = new ConcurrentHashMap<>();

    public void recordFailed(String email) {
        cache.merge(
            email.toLowerCase(),
            LoginAttemptInfo.first(),
            (old, val) -> old.isBlocked() ? old : old.increment()
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
