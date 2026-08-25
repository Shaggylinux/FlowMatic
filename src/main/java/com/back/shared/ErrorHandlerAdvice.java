package com.back.shared;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.back.shared.exception.ClaveCortaException;
import com.back.shared.exception.DominioException;
import com.back.shared.exception.UsuarioDuplicadoException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ErrorHandlerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlerAdvice.class);

    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        return uri.startsWith("/api/") || uri.startsWith("/admin/api/") || uri.startsWith("/calendario/candidato/")
                || "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        logger.warn("Carga de archivo rechazada por tamaño excedido en {}: {}", request.getRequestURI(), ex.getMessage());
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
            body.put("error", "El archivo supera el tamaño máximo permitido de 30 MB");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
        }
        return "redirect:/drive?error=" + URLEncoder.encode("El archivo supera el tamaño máximo permitido de 30 MB",
                StandardCharsets.UTF_8);
    }

    @ExceptionHandler(UsuarioDuplicadoException.class)
    public Object handleUsuarioDuplicado(UsuarioDuplicadoException ex, HttpServletRequest request) {
        logger.warn("Intento de registro con usuario/email duplicado en {}: {}", request.getRequestURI(), ex.getMessage());
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.CONFLICT.value());
            body.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        return "redirect:/registro?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
    }

    @ExceptionHandler(ClaveCortaException.class)
    public Object handleClaveCorta(ClaveCortaException ex, HttpServletRequest request) {
        logger.warn("Contraseña no cumple requisitos de seguridad en {}: {}", request.getRequestURI(), ex.getMessage());
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
        return "redirect:/registro?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
    }

    @ExceptionHandler(DominioException.class)
    public Object handleDominio(DominioException ex, HttpServletRequest request) {
        logger.warn("Excepción de dominio en {}: {}", request.getRequestURI(), ex.getMessage());
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
            body.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
        }
        return "redirect:/?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Argumento inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
        return "redirect:/?error=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception ex, HttpServletRequest request) {
        logger.error("Error no controlado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        if (isApiRequest(request)) {
            Map<String, Object> body = new HashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            body.put("error", "Error interno del servidor. Por favor, intenta más tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
        return "redirect:/?error=" + URLEncoder.encode("Ha ocurrido un error inesperado.", StandardCharsets.UTF_8);
    }
}