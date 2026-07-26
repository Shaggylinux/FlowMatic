package com.back.shared;

import java.time.LocalDateTime;

public record LoginAttemptInfo(int attempts, LocalDateTime blockedUntil) {
    public boolean isBlocked() {
        return blockedUntil != null && LocalDateTime.now().isBefore(blockedUntil);
    }

    public LoginAttemptInfo increment() {
        int next = attempts + 1;
        if (next >= 5) return new LoginAttemptInfo(0, LocalDateTime.now().plusMinutes(15));
        return new LoginAttemptInfo(next, null);
    }

    public static LoginAttemptInfo first() {
        return new LoginAttemptInfo(1, null);
    }
}
