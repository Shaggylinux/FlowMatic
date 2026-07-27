package com.back.shared;

import com.back.calendario.Evento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${mail.from:FLOWMATIC <malacruz132@gmail.com>}")
    private String mailFrom;

    public boolean enviarEmailVerificacion(String destinatario, String nombre, String token) {
        try {
            logger.info("📧 Preparando email de verificación para: {}", destinatario);

            String enlaceActivacion = "http://localhost:" + serverPort + "/registro/candidato/activar?token=" + token;

            String asunto = "✅ Activa tu cuenta en FLOWMATIC";
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("enlaceActivacion", enlaceActivacion);

            String mensaje = templateEngine.process("emails/email-verificacion", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email enviado exitosamente a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de verificación: {}", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean enviarEmailRecuperacion(String destinatario, String nombre, String token) {
        try {
            logger.info("📧 Preparando email de recuperación para: {}", destinatario);

            String enlace = "http://localhost:" + serverPort + "/reset-password?token=" + token;

            String asunto = "🔐 Restablece tu contraseña en FLOWMATIC";
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("enlace", enlace);

            String mensaje = templateEngine.process("emails/email-recuperacion", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email de recuperación enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de recuperación: {}", e.getMessage());
            return false;
        }
    }

    public boolean enviarEmailEntrevista(String destinatario, String nombreRRHH, Evento evento, String candidatoNombre) {
        try {
            logger.info("📧 Preparando email de confirmación de entrevista para: {}", destinatario);

            String asunto = "📅 Nueva entrevista agendada - " + candidatoNombre;

            Context context = new Context();
            context.setVariable("nombreRRHH", nombreRRHH);
            context.setVariable("candidatoNombre", candidatoNombre);
            context.setVariable("fecha", evento.getFecha() != null ? evento.getFecha().toString() : "");
            context.setVariable("hora", evento.getHora() != null ? evento.getHora().toString() : "");
            context.setVariable("tipo", evento.getTipo() != null ? evento.getTipo() : "ENTREVISTA_INICIAL");
            context.setVariable("lugar", evento.getLugar());
            context.setVariable("observaciones", evento.getObservaciones());

            String mensaje = templateEngine.process("emails/email-entrevista", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email de entrevista enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de entrevista: {}", e.getMessage());
            return false;
        }
    }
}
