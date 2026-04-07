package com.back.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    public boolean enviarEmailVerificacion(String destinatario, String nombre, String token) {
        try {
            logger.info("📧 Preparando email de verificación para: {}", destinatario);

            String enlaceActivacion = "http://localhost:" + serverPort + "/registro/candidato/activar?token=" + token;

            String asunto = "Verificación de cuenta - FlowMatic";
            String mensaje = "Hola " + nombre + ",\n\n" +
                    "¡Bienvenido a FlowMatic!\n\n" +
                    "Tu código de activación es: " + token + "\n\n" +
                    "O haz clic en el siguiente enlace para activar tu cuenta:\n" +
                    enlaceActivacion + "\n\n" +
                    "Este enlace expira en 24 horas.\n\n" +
                    "Si no registraste esta cuenta, ignora este correo.\n\n" +
                    "Saludos,\nEquipo de FlowMatic";

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);
            email.setFrom("noreply@flowmatic.com");

            mailSender.send(email);

            logger.info("Email enviado exitosamente a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("rror al enviar email: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarEmailRecuperacion(String destinatario, String nombre, String token) {
        try {
            logger.info("📧 Preparando email de recuperación para: {}", destinatario);

            String enlace = "http://localhost:" + serverPort + "/reset-password?token=" + token;

            String asunto = "Recuperación de contraseña - FlowMatic";
            String mensaje = "Hola " + nombre + ",\n\n" +
                    "Recibimos una solicitud para restablecer tu contraseña.\n\n" +
                    "Haz clic en el siguiente enlace:\n" +
                    enlace + "\n\n" +
                    "Si no solicitaste esto, ignora este mensaje.\n\n" +
                    "Saludos,\nEquipo de FlowMatic";

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(destinatario);
            email.setSubject(asunto);
            email.setText(mensaje);
            email.setFrom("noreply@flowmatic.com");

            mailSender.send(email);

            logger.info("Email de recuperación enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de recuperación: {}", e.getMessage());
            return false;
        }
    }
}
