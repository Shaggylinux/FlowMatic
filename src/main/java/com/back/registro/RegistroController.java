package com.back.registro;

import com.back.shared.exception.ClaveCortaException;
import com.back.shared.exception.UsuarioDuplicadoException;
import com.back.admin.ConfiguracionService;
import com.back.shared.dto.RegistroUsuarioDTO;
import com.back.auth.Usuario;
import com.back.auth.UsuarioService;
import jakarta.validation.Valid;

import java.time.LocalDateTime;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/registro/candidato")
@RequiredArgsConstructor
public class RegistroController {

    private final UsuarioService usuarioService;
    private final ConfiguracionService configuracionService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("registro", new RegistroRequest());
        return "registro-candidato";
    }

    @PostMapping
    public String procesarRegistro(
        @Valid @ModelAttribute("registro") RegistroRequest registro,
        BindingResult resultado,
        Model model,
        @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {

    if (resultado.hasErrors()) {
        return "registro-candidato";
    }

    RegistroUsuarioDTO dto = RegistroUsuarioDTO.builder()
        .email(registro.getEmail())
        .clave(registro.getClave())
        .rol("ROLE_CANDIDATO")
        .username(registro.getUsername())
        .apellido(registro.getApellido())
        .build();

    try {
        usuarioService.registrarUsuario(dto);
    } catch (UsuarioDuplicadoException e) {
        model.addAttribute("errorDuplicado", true);
        return "registro-candidato";
    } catch (ClaveCortaException e) {
        model.addAttribute("errorClaveCorta", true);
        return "registro-candidato";
    }

    if ("XMLHttpRequest".equals(requestedWith)) {
        return "fragments/success-message :: success";
    }

    return "redirect:/registro/candidato?pendiente";
}

    @GetMapping(params = "pendiente")
    public String registropendiente(Model model){
        model.addAttribute("registro", new RegistroRequest());
        model.addAttribute("mensajePendiente", true);
        return "registro-candidato";
    }

    @GetMapping("/activar")
    public String activarCuenta(@RequestParam("token") String token, Model model) {
        Usuario usuario = usuarioService.buscarPorToken(token);

        if (usuario == null) {
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
            model.addAttribute("registro", new RegistroRequest());
            return "registro-candidato";
        }
    }

    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<?> registrarDesdeModal(@Valid @RequestBody RegistroRequest registro, BindingResult resultado) {
        if (resultado.hasErrors()) {
            return ResponseEntity.badRequest().body("Datos inv\u00e1lidos");
        }

        RegistroUsuarioDTO dto = RegistroUsuarioDTO.builder()
            .email(registro.getEmail())
            .clave(registro.getClave())
            .rol("ROLE_CANDIDATO")
            .username(registro.getUsername())
            .apellido(registro.getApellido())
            .build();

        try {
            usuarioService.registrarUsuario(dto);
        } catch (UsuarioDuplicadoException e) {
            return ResponseEntity.status(409).body("El usuario ya existe");
        } catch (ClaveCortaException e) {
            return ResponseEntity.badRequest().body("La contrase\u00f1a debe tener m\u00ednimo 8 caracteres");
        }

    return ResponseEntity.ok().build();
    }
}
