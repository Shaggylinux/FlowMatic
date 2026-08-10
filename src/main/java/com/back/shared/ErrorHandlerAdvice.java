package com.back.shared;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ErrorHandlerAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return "redirect:/drive?error=" + URLEncoder.encode("El archivo supera el tamaño máximo permitido de 30 MB",
                StandardCharsets.UTF_8);
    }
}