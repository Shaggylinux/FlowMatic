package com.back.notificaciones;

import com.back.shared.dto.EntrevistaEmailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;

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

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${mail.from:FLOWMATIC <malacruz132@gmail.com>}")
    private String mailFrom;

    public boolean enviarEmailVerificacion(String destinatario, String nombre, String token) {
        return enviarEmailVerificacion(destinatario, nombre, null, token);
    }

    public boolean enviarEmailVerificacion(String destinatario, String nombre, String clave, String token) {
        try {
            logger.info("📧 Preparando email de verificación para: {}", destinatario);

            String enlaceActivacion = appBaseUrl + contextPath + "/activar-cuenta?token=" + token;

            String asunto = "✅ Activa tu cuenta en FLOWMATIC";
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("email", destinatario);
            context.setVariable("clave", clave != null && !clave.isBlank() ? clave : null);
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
            return false;
        }
    }

    public boolean enviarEmailBienvenidaRRHH(String destinatario, String nombre, String apellido, String clave, String token) {
        try {
            logger.info("📧 Preparando email de bienvenida RRHH para: {}", destinatario);

            String nombreCompleto = nombre + (apellido != null && !apellido.isBlank() ? " " + apellido : "");
            String enlaceActivacion = appBaseUrl + contextPath + "/activar-cuenta?token=" + token;
            String enlaceLogin = appBaseUrl + contextPath + "/login";

            String asunto = "🎉 Bienvenido al equipo de RRHH — Activa tu cuenta en FLOWMATIC";
            Context context = new Context();
            context.setVariable("nombre", nombreCompleto);
            context.setVariable("email", destinatario);
            context.setVariable("clave", clave != null && !clave.isBlank() ? clave : "Asignada por el Administrador");
            context.setVariable("enlaceActivacion", enlaceActivacion);
            context.setVariable("enlaceLogin", enlaceLogin);

            String mensaje = templateEngine.process("emails/email-bienvenida-rrhh", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email de bienvenida RRHH enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de bienvenida RRHH: {}", e.getMessage());
            return false;
        }
    }

    public boolean enviarEmailRecuperacion(String destinatario, String nombre, String token) {
        try {
            logger.info("📧 Preparando email de recuperación para: {}", destinatario);

            String enlace = appBaseUrl + contextPath + "/recuperar-contrasena?token=" + token;

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
            throw new RuntimeException("Error al enviar el email de recuperación", e);
        }
    }

    public boolean enviarEmailBloqueo(String destinatario, String nombre, long minutos) {
        try {
            logger.info("📧 Preparando email de bloqueo para: {}", destinatario);

            String asunto = "🔒 Acceso bloqueado temporalmente - FLOWMATIC";
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("minutos", minutos);

            String mensaje = templateEngine.process("emails/email-bloqueo", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email de bloqueo enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de bloqueo: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el email de bloqueo", e);
        }
    }

    public boolean enviarEmailAccionCandidato(String destinatario, String rrhhNombre,
                                              String accion, String candidatoNombre,
                                              LocalDate fecha, LocalTime hora,
                                              LocalDate nuevaFecha, LocalTime nuevaHora,
                                              String motivo) {
        try {
            logger.info("📧 Preparando email de accion del candidato ({}) para: {}", accion, destinatario);

            String asunto = "🔔 " + candidatoNombre + " - " +
                    ("CONFIRMACION".equals(accion) ? "confirm\u00f3 su asistencia" : "solicita reprogramar su entrevista");

            Context context = new Context();
            context.setVariable("rrhhNombre", rrhhNombre);
            context.setVariable("candidatoNombre", candidatoNombre);
            context.setVariable("accion", accion);
            context.setVariable("fecha", fecha != null ? fecha.toString() : "");
            context.setVariable("hora", hora != null ? hora.toString() : "");
            context.setVariable("nuevaFecha", nuevaFecha != null ? nuevaFecha.toString() : "");
            context.setVariable("nuevaHora", nuevaHora != null ? nuevaHora.toString() : "");
            context.setVariable("motivo", motivo != null ? motivo : "");

            String mensaje = templateEngine.process("emails/email-accion-candidato", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email de accion del candidato enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de accion del candidato: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el email de accion del candidato", e);
        }
    }

    public boolean enviarEmailEntrevista(String destinatario, String nombreRRHH, EntrevistaEmailDTO evento, String candidatoNombre) {
        try {
            logger.info("📧 Preparando email de confirmación de entrevista para: {}", destinatario);

            String asunto = "📅 Nueva entrevista agendada - " + candidatoNombre;

            Context context = new Context();
            context.setVariable("nombreRRHH", nombreRRHH);
            context.setVariable("candidatoNombre", candidatoNombre);
            context.setVariable("fecha", evento.fecha() != null ? evento.fecha().toString() : "");
            context.setVariable("hora", evento.hora() != null ? evento.hora().toString() : "");
            context.setVariable("tipo", evento.tipo() != null ? evento.tipo() : "ENTREVISTA_INICIAL");
            context.setVariable("lugar", evento.lugar());
            context.setVariable("observaciones", evento.observaciones());
            context.setVariable("modalidad", evento.modalidad());
            context.setVariable("entrevistador", evento.entrevistador());

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
            throw new RuntimeException("Error al enviar el email de entrevista", e);
        }
    }

    public boolean enviarEmailEntrevistaCandidato(String destinatario, String candidatoNombre, EntrevistaEmailDTO evento) {
        try {
            logger.info("📧 Preparando email de entrevista para el candidato: {}", destinatario);

            String asunto = "📅 Programación de tu entrevista - " + candidatoNombre;

            Context context = new Context();
            context.setVariable("candidatoNombre", candidatoNombre);
            context.setVariable("fecha", evento.fecha() != null ? evento.fecha().toString() : "");
            context.setVariable("hora", evento.hora() != null ? evento.hora().toString() : "");
            context.setVariable("tipo", evento.tipo() != null ? evento.tipo() : "ENTREVISTA_INICIAL");
            context.setVariable("lugar", evento.lugar());
            context.setVariable("observaciones", evento.observaciones());
            context.setVariable("modalidad", evento.modalidad());
            context.setVariable("entrevistador", evento.entrevistador());

            String mensaje = templateEngine.process("emails/email-entrevista-candidato", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom(mailFrom);

            mailSender.send(mimeMessage);

            logger.info("Email de entrevista enviado al candidato: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de entrevista al candidato: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el email de entrevista al candidato", e);
        }
    }
}
