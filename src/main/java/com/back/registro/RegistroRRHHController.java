package com.back.registro;

import com.back.shared.exception.ClaveCortaException;
import com.back.shared.exception.UsuarioDuplicadoException;
import com.back.shared.api.AuthApi;
import com.back.shared.dto.RegistroUsuarioDTO;
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

    private final AuthApi authApi;

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

        if (registro.getConfirmarClave() != null && !registro.getClave().equals(registro.getConfirmarClave())) {
            model.addAttribute("errorClaveNoCoincide", true);
            return "registro-rrhh";
        }

        if (resultado.hasErrors()) {
            return "registro-rrhh";
        }

        RegistroUsuarioDTO dto = RegistroUsuarioDTO.builder()
            .email(registro.getEmail())
            .clave(registro.getClave())
            .rol("ROLE_RRHH")
            .username(registro.getUsername())
            .apellido(registro.getApellido())
            .build();

        try {
            authApi.registrarUsuario(dto);
        } catch (UsuarioDuplicadoException e) {
            model.addAttribute("errorDuplicado", true);
            return "registro-rrhh";
        } catch (ClaveCortaException e) {
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
