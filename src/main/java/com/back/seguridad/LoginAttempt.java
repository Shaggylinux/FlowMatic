package com.back.seguridad;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_attempts")
@NoArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;

    public LoginAttempt(String email) {
        this.email = email;
    }
}
