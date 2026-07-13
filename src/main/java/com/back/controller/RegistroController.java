package com.back.controller;

import com.back.model.Usuario;
import com.back.service.UsuarioService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/registro/candidato")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro-candidato";
    }

    @PostMapping
    public String procesarRegistro(
        @Valid @ModelAttribute("usuario") Usuario usuario,
        BindingResult resultado,
        Model model,
        @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

    if (usuario.getRol() == null || usuario.getRol().isEmpty()){
        usuario.setRol("ROLE_CANDIDATO");
    }

    if (resultado.hasErrors()) {
        return "registro-candidato";
    }

    String respuesta = usuarioService.registrarUsuario(usuario);

    if ("DUPLICADO".equals(respuesta)) {
        model.addAttribute("errorDuplicado", true);
        return "registro-candidato";
    }

    if ("XMLHttpRequest".equals(requestedWith)) {
        return "fragments/success-message :: success";
    }

    return "redirect:/registro/candidato?pendiente";
}

    @GetMapping(params = "pendiente")
    public String registropendiente(Model model){
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("mensajePendiente", true);
        return "registro-candidato";
    }

    @GetMapping("/activar")
    public String activarCuenta(@RequestParam("token") String token, Model model) {
        Usuario usuario = usuarioService.buscarPorToken(token);

        if (usuario == null) {
            model.addAttribute("tokenInvalido", true);
            return "activacion";
        }

        long segundos = java.time.Duration.between(usuario.getFechaCreacionToken(), LocalDateTime.now()).getSeconds();

        if (segundos > 15) {
            model.addAttribute("enlaceExpirado", true);
            model.addAttribute("token", token);
            return "caduco";
        }

        boolean activado = usuarioService.activarCuenta(token);
        model.addAttribute("activacionExitosa", activado);
        return "activacion";
    }

    @PostMapping("/reenviar-activacion")
    public String reenviarActivacion(@RequestParam("token") String token, Model model) {
        usuarioService.regenerarYReenviarToken(token);
        model.addAttribute("correoReenviado", true);
        return "home";
    }

    @PostMapping("/verificar")
    public String verificarSms(
            @RequestParam("token") String token,
            Model model
    ){
        boolean activado = usuarioService.activarCuenta(token);

        if (activado){
            model.addAttribute("activacionExitosa", true);
            return "activacion";
        } else {
            model.addAttribute("mensajePendiente", true);
            model.addAttribute("errorVerificacion", true);
            model.addAttribute("usuario", new Usuario());
            return "registro-candidato";
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> registrarDesdeModal(@Valid @RequestBody Usuario usuario, BindingResult resultado) {
        usuario.setRol("ROLE_CANDIDATO");

        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body("Datos inválidos");
        }

        if (usuario.getRol() == null || usuario.getRol().isEmpty()){
            usuario.setRol("ROLE_CANDIDATO");
        }

        String respuesta = usuarioService.registrarUsuario(usuario);

        if ("DUPLICADO".equals(respuesta)) {
            return ResponseEntity.status(409).body("El usuario ya existe");
        }

    return ResponseEntity.ok().build();
    }
}