package com.back.seguridad;

import java.time.LocalDateTime;

public record LoginAttemptInfo(int attempts, LocalDateTime blockedUntil) {
    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    public LoginAttemptInfo increment(int maxAttempts, long blockMinutes) {
        int next = attempts + 1;
        if (next >= maxAttempts) return new LoginAttemptInfo(0, LocalDateTime.now().plusMinutes(blockMinutes));
        return new LoginAttemptInfo(next, null);
    }

    public static LoginAttemptInfo first() {
        return new LoginAttemptInfo(1, null);
    }
}
