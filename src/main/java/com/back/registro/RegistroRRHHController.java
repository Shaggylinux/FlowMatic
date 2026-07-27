package com.back.registro;

import com.back.auth.Usuario;
import com.back.auth.UsuarioService;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/registro/rrhh")
@RequiredArgsConstructor
public class RegistroRRHHController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String mostrarFormulario(Model model) {
        model.addAttribute("registro", new RegistroRequest());
        return "registro-rrhh";
    }

    @PostMapping
    public String procesarRegistro(
        @Valid @ModelAttribute("registro") RegistroRequest registro,
        BindingResult resultado,
        Model model) {

        if (resultado.hasErrors()) {
            return "registro-rrhh";
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(registro.getEmail());
        usuario.setClave(registro.getClave());
        usuario.setRol("ROLE_RRHH");

        String respuesta = usuarioService.registrarUsuario(usuario, registro.getUsername(), registro.getApellido(), null);

        if ("DUPLICADO".equals(respuesta)) {
            model.addAttribute("errorDuplicado", true);
            return "registro-rrhh";
        }

        if ("CLAVE_CORTA".equals(respuesta)) {
            model.addAttribute("errorClaveCorta", true);
            return "registro-rrhh";
        }

        return "redirect:/registro/rrhh?exito";
    }

    @GetMapping(params = "exito")
    public String registroExitoso(Model model){
        model.addAttribute("registro", new RegistroRequest());
        model.addAttribute("mensajeExito", true);
        return "registro-rrhh";
    }
}
