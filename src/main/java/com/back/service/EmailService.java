package com.back.service;

import com.back.model.Evento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
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

            String asunto = "✅ Activa tu cuenta en FLOWMATIC";
            String mensaje = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f8f9fa; border-radius: 10px;'>" +
                    "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;'>" +
                    "<h1 style='color: white; margin: 0; font-size: 28px;'>FLOWMATIC</h1>" +
                    "<p style='color: #e0e0e0; margin: 10px 0 0 0;'>Sistema de Gestión de Candidatos</p>" +
                    "</div>" +
                    "<div style='background: white; padding: 30px; border-radius: 0 0 10px 10px;'>" +
                    "<h2 style='color: #333;'>¡Bienvenido, " + nombre + "!</h2>" +
                    "<p style='color: #666; line-height: 1.6;'>Gracias por registrarte en <strong>FLOWMATIC</strong>. Estamos encantado de tenerte con nosotros.</p>" +
                    "<p style='color: #666; line-height: 1.6;'>Para completar tu registro, necesitas activar tu cuenta. Haz clic en el siguiente botón:</p>" +
                    "<div style='text-align: center; margin: 30px 0;'>" +
                    "<a href='" + enlaceActivacion + "' style='background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Activar mi cuenta</a>" +
                    "</div>" +
                    "<p style='color: #999; font-size: 12px; text-align: center;'>O copia y pega este enlace en tu navegador:</p>" +
                    "<p style='color: #667eea; font-size: 12px; text-align: center; word-break: break-all;'>" + enlaceActivacion + "</p>" +
                    "<p style='color: #999; font-size: 12px; margin-top: 20px;'>Este enlace expires en 24 horas.</p>" +
                    "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<p style='color: #666; font-size: 12px;'>Si no creaste esta cuenta, puedes ignorar este correo sin preocupaciones.</p>" +
                    "<p style='color: #666; font-size: 12px; margin-top: 20px;'>Saludos cordiales,<br><strong>Equipo de FLOWMATIC</strong></p>" +
                    "</div>" +
                    "</div>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom("FLOWMATIC <malacruz132@gmail.com>");

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
            String mensaje = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f8f9fa; border-radius: 10px;'>" +
                    "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;'>" +
                    "<h1 style='color: white; margin: 0; font-size: 28px;'>FLOWMATIC</h1>" +
                    "<p style='color: #e0e0e0; margin: 10px 0 0 0;'>Sistema de Gestión de Candidatos</p>" +
                    "</div>" +
                    "<div style='background: white; padding: 30px; border-radius: 0 0 10px 10px;'>" +
                    "<h2 style='color: #333;'>¿Olvidaste tu contraseña, " + nombre + "?</h2>" +
                    "<p style='color: #666; line-height: 1.6;'>Recibimos una solicitud para restablecer la contraseña de tu cuenta en FLOWMATIC.</p>" +
                    "<p style='color: #666; line-height: 1.6;'>Haz clic en el siguiente botón para crear una nueva contraseña:</p>" +
                    "<div style='text-align: center; margin: 30px 0;'>" +
                    "<a href='" + enlace + "' style='background: #667eea; color: white; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;'>Restablecer contraseña</a>" +
                    "</div>" +
                    "<p style='color: #999; font-size: 12px; text-align: center;'>O copia y pega este enlace en tu navegador:</p>" +
                    "<p style='color: #667eea; font-size: 12px; text-align: center; word-break: break-all;'>" + enlace + "</p>" +
                    "<p style='color: #999; font-size: 12px; margin-top: 20px;'>Este enlace expirará en 1 hora.</p>" +
                    "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<p style='color: #666; font-size: 12px;'>Si no solicitaste el restablecimiento de contraseña, puedes ignorar este correo. Tu contraseña actual seguirá siendo válida.</p>" +
                    "<p style='color: #666; font-size: 12px; margin-top: 20px;'>Saludos cordiales,<br><strong>Equipo de FLOWMATIC</strong></p>" +
                    "</div>" +
                    "</div>";

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(mensaje, true);
            helper.setFrom("FLOWMATIC <malacruz132@gmail.com>");

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

            StringBuilder sb = new StringBuilder();
            sb.append("<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f8f9fa; border-radius: 10px;\">")
              .append("<div style=\"background: linear-gradient(135deg, #0EA5A5 0%, #0F172A 100%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;\">")
              .append("<h1 style=\"color: white; margin: 0; font-size: 28px;\">FLOWMATIC</h1>")
              .append("<p style=\"color: #e0e0e0; margin: 10px 0 0 0;\">Sistema de Gesti\u00f3n de Candidatos</p>")
              .append("</div>")
              .append("<div style=\"background: white; padding: 30px; border-radius: 0 0 10px 10px;\">")
              .append("<h2 style=\"color: #333;\">\u00a1Hola, ").append(nombreRRHH).append("!</h2>")
              .append("<p style=\"color: #666; line-height: 1.6;\">Se ha agendado una nueva entrevista con los siguientes detalles:</p>")
              .append("<table style=\"width: 100%; border-collapse: collapse; margin: 20px 0;\">")
              .append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #999; width: 120px;\">Candidato</td><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #333; font-weight: 600;\">").append(candidatoNombre).append("</td></tr>")
              .append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #999;\">Fecha</td><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #333; font-weight: 600;\">").append(evento.getFecha().toString()).append("</td></tr>")
              .append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #999;\">Hora</td><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #333; font-weight: 600;\">").append(evento.getHora().toString()).append("</td></tr>")
              .append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #999;\">Tipo</td><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #333; font-weight: 600;\">").append(evento.getTipo() != null ? evento.getTipo() : "ENTREVISTA_INICIAL").append("</td></tr>");

            if (evento.getLugar() != null && !evento.getLugar().isEmpty()) {
                sb.append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #999;\">Lugar</td><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #333; font-weight: 600;\">").append(evento.getLugar()).append("</td></tr>");
            }

            if (evento.getObservaciones() != null && !evento.getObservaciones().isEmpty()) {
                sb.append("<tr><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #999;\">Observaciones</td><td style=\"padding: 10px; border-bottom: 1px solid #eee; color: #333;\">").append(evento.getObservaciones()).append("</td></tr>");
            }

            sb.append("</table>")
              .append("<p style=\"color: #999; font-size: 12px; margin-top: 20px;\">Puedes gestionar esta entrevista desde el calendario en FLOWMATIC.</p>")
              .append("<hr style=\"border: none; border-top: 1px solid #eee; margin: 20px 0;\">")
              .append("<p style=\"color: #666; font-size: 12px; margin-top: 20px;\">Saludos cordiales,<br><strong>Equipo de FLOWMATIC</strong></p>")
              .append("</div></div>");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(sb.toString(), true);
            helper.setFrom("FLOWMATIC <malacruz132@gmail.com>");

            mailSender.send(mimeMessage);

            logger.info("Email de entrevista enviado a: {}", destinatario);
            return true;

        } catch (Exception e) {
            logger.error("Error al enviar email de entrevista: {}", e.getMessage());
            return false;
        }
    }
}
