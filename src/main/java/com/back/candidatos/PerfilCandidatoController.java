package com.back.candidatos;

import com.back.auth.Usuario;
import com.back.auth.UsuarioService;
import com.back.notificaciones.Notificacion;
import com.back.notificaciones.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class PerfilCandidatoController {

    private final UsuarioService usuarioService;
    private final CandidatoService candidatoService;
    private final NotificacionService notificacionService;

    @GetMapping("/candidato/perfil")
    public String vistaPerfil(Authentication auth, Model model) {
        String email = auth != null ? auth.getName() : null;

        String primerNombre = "";
        String nombres = "";
        String apellidos = "";
        String nombreCompleto = "";
        String iniciales = "C";
        String estado = "Registrado";
        String cargo = "";
        String ciudad = "";
        String telefono = "";
        String telefonoFijo = "";
        String direccion = "";
        String tipoDocumento = "Cédula de ciudadanía";
        String numeroDocumento = "";
        String genero = "No especificado";
        String estadoCivil = "Soltero/a";
        String fechaNacimientoStr = "";
        String fechaNacimientoIso = "";
        String nacionalidad = "Colombiana";
        String sobreMi = "";
        String tecnologias = "";
        String idiomas = "";
        String areaProfesional = "";
        String pretensionSalarial = "";
        String disponibilidad = "";
        String modalidadTrabajo = "";
        String formacionJson = "[]";
        String experienciaJson = "[]";
        String idiomasJson = "[]";

        Long candidatoId = null;

        if (email != null) {
            Optional<Usuario> uOpt = usuarioService.buscarPorEmail(email);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                candidatoId = u.getId();
                Optional<Candidato> cOpt = candidatoService.buscarPorId(u.getId());
                if (cOpt.isPresent()) {
                    Candidato c = cOpt.get();
                    if (c.getNombres() != null && !c.getNombres().isBlank()) {
                        nombres = c.getNombres();
                        primerNombre = c.getNombres().split(" ")[0];
                    } else if (c.getUsername() != null && !c.getUsername().isBlank()) {
                        nombres = c.getUsername();
                        primerNombre = c.getUsername().split(" ")[0];
                    }
                    if (c.getApellido() != null) apellidos = c.getApellido();
                    nombreCompleto = (nombres + " " + apellidos).trim();

                    if (c.getEstado() != null) estado = c.getEstado();
                    if (c.getCargo() != null) cargo = c.getCargo();
                    if (c.getCiudad() != null) ciudad = c.getCiudad();
                    if (c.getTelefono() != null) telefono = c.getTelefono();
                    if (c.getTelefonoFijo() != null) telefonoFijo = c.getTelefonoFijo();
                    if (c.getDireccion() != null) direccion = c.getDireccion();
                    if (c.getTipoDocumento() != null) tipoDocumento = c.getTipoDocumento();
                    if (c.getNumeroDocumento() != null) numeroDocumento = c.getNumeroDocumento();
                    if (c.getGenero() != null) genero = c.getGenero();
                    if (c.getEstadoCivil() != null) estadoCivil = c.getEstadoCivil();
                    if (c.getFechaNacimiento() != null) {
                        DateTimeFormatter fEs = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"));
                        fechaNacimientoStr = c.getFechaNacimiento().format(fEs);
                        fechaNacimientoIso = c.getFechaNacimiento().toString();
                    }
                    if (c.getNacionalidad() != null) nacionalidad = c.getNacionalidad();
                    if (c.getSobreMi() != null) sobreMi = c.getSobreMi();
                    if (c.getTecnologias() != null) tecnologias = c.getTecnologias();
                    if (c.getIdiomas() != null) idiomas = c.getIdiomas();
                    if (c.getAreaProfesional() != null) areaProfesional = c.getAreaProfesional();
                    if (c.getPretensionSalarial() != null) pretensionSalarial = c.getPretensionSalarial();
                    if (c.getDisponibilidad() != null) disponibilidad = c.getDisponibilidad();
                    if (c.getModalidadTrabajo() != null) modalidadTrabajo = c.getModalidadTrabajo();
                    if (c.getFormacionJson() != null && !c.getFormacionJson().isBlank()) formacionJson = c.getFormacionJson();
                    if (c.getExperienciaJson() != null && !c.getExperienciaJson().isBlank()) experienciaJson = c.getExperienciaJson();
                    if (c.getIdiomasJson() != null && !c.getIdiomasJson().isBlank()) idiomasJson = c.getIdiomasJson();
                } else {
                    primerNombre = email.split("@")[0];
                    nombres = primerNombre;
                    nombreCompleto = primerNombre;
                    iniciales = primerNombre.substring(0, 1).toUpperCase();
                }
            }
        }

        // Consultar notificaciones reales
        List<Notificacion> notificaciones = candidatoId != null
                ? notificacionService.obtenerActividadReciente(candidatoId)
                : notificacionService.obtenerActividadReciente();
        long notificacionesNoLeidas = candidatoId != null
                ? notificacionService.contarNoLeidasPorCandidato(candidatoId)
                : notificacionService.contarNoLeidasGlobales();

        model.addAttribute("candidatoEmail", email != null ? email : "");
        model.addAttribute("candidatoPrimerNombre", primerNombre);
        model.addAttribute("candidatoNombres", nombres);
        model.addAttribute("candidatoApellidos", apellidos);
        model.addAttribute("candidatoNombreCompleto", nombreCompleto);
        model.addAttribute("usuarioIniciales", iniciales);
        model.addAttribute("candidatoEstado", estado);
        model.addAttribute("candidatoCargo", cargo);
        model.addAttribute("candidatoCiudad", ciudad);
        model.addAttribute("candidatoTelefono", telefono);
        model.addAttribute("candidatoTelefonoFijo", telefonoFijo);
        model.addAttribute("candidatoDireccion", direccion);
        model.addAttribute("candidatoTipoDoc", tipoDocumento);
        model.addAttribute("candidatoNumDoc", numeroDocumento);
        model.addAttribute("candidatoGenero", genero);
        model.addAttribute("candidatoEstadoCivil", estadoCivil);
        model.addAttribute("candidatoFechaNac", fechaNacimientoStr);
        model.addAttribute("candidatoFechaNacIso", fechaNacimientoIso);
        model.addAttribute("candidatoNacionalidad", nacionalidad);
        model.addAttribute("candidatoSobreMi", sobreMi);
        model.addAttribute("candidatoTecnologias", tecnologias);
        model.addAttribute("candidatoIdiomas", idiomas);
        model.addAttribute("candidatoAreaProfesional", areaProfesional);
        model.addAttribute("candidatoPretensionSalarial", pretensionSalarial);
        model.addAttribute("candidatoDisponibilidad", disponibilidad);
        model.addAttribute("candidatoModalidadTrabajo", modalidadTrabajo);
        model.addAttribute("formacionJson", formacionJson);
        model.addAttribute("experienciaJson", experienciaJson);
        model.addAttribute("idiomasJson", idiomasJson);

        model.addAttribute("notificaciones", notificaciones);
        model.addAttribute("notificacionesNoLeidas", notificacionesNoLeidas);

        return "candidato-perfil";
    }

    @PostMapping("/candidato/perfil/actualizar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> actualizarPerfil(Authentication auth, @RequestBody PerfilCandidatoDTO dto) {
        Map<String, Object> resp = new HashMap<>();
        String email = auth != null ? auth.getName() : null;

        if (email == null) {
            resp.put("success", false);
            resp.put("message", "Usuario no autenticado");
            return ResponseEntity.status(401).body(resp);
        }

        Optional<Usuario> uOpt = usuarioService.buscarPorEmail(email);
        if (uOpt.isPresent()) {
            Usuario u = uOpt.get();

            if (dto.getNombres() != null && !dto.getNombres().isBlank() && !dto.getNombres().trim().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
                resp.put("success", false);
                resp.put("message", "Los nombres deben contener solo letras (de 2 a 50 caracteres)");
                return ResponseEntity.badRequest().body(resp);
            }

            if (dto.getApellido() != null && !dto.getApellido().isBlank() && !dto.getApellido().trim().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
                resp.put("success", false);
                resp.put("message", "Los apellidos deben contener solo letras (de 2 a 50 caracteres)");
                return ResponseEntity.badRequest().body(resp);
            }

            if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
                String tel = dto.getTelefono().trim();
                if (!tel.matches("^[0-9]{10}$")) {
                    resp.put("success", false);
                    resp.put("message", "El teléfono celular debe contener exactamente 10 dígitos numéricos");
                    return ResponseEntity.badRequest().body(resp);
                }
            }

            if (dto.getTelefonoFijo() != null && !dto.getTelefonoFijo().isBlank()) {
                String telFijo = dto.getTelefonoFijo().trim();
                if (!telFijo.matches("^[0-9]{7,10}$")) {
                    resp.put("success", false);
                    resp.put("message", "El teléfono fijo debe contener entre 7 y 10 dígitos numéricos");
                    return ResponseEntity.badRequest().body(resp);
                }
            }

            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isBlank()) {
                String doc = dto.getNumeroDocumento().trim();
                if (!doc.matches("^[0-9]{6,10}$")) {
                    resp.put("success", false);
                    resp.put("message", "El documento debe contener solo números (de 6 a 10 dígitos)");
                    return ResponseEntity.badRequest().body(resp);
                }
            }

            if (dto.getCargo() != null && !dto.getCargo().isBlank() && !dto.getCargo().trim().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,100}$")) {
                resp.put("success", false);
                resp.put("message", "El cargo debe contener solo letras y espacios");
                return ResponseEntity.badRequest().body(resp);
            }

            if (dto.getCiudad() != null && !dto.getCiudad().isBlank() && !dto.getCiudad().trim().matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{2,50}$")) {
                resp.put("success", false);
                resp.put("message", "La ciudad debe contener solo letras y espacios");
                return ResponseEntity.badRequest().body(resp);
            }

            Candidato c = candidatoService.buscarPorId(u.getId()).orElseGet(() -> {
                Candidato nuevo = new Candidato();
                nuevo.setId(u.getId());
                nuevo.setUsername(dto.getNombres() != null ? dto.getNombres().trim() : u.getEmail());
                nuevo.setApellido(dto.getApellido() != null ? dto.getApellido().trim() : "");
                return nuevo;
            });

            if (dto.getNombres() != null && !dto.getNombres().isBlank()) {
                c.setNombres(dto.getNombres().trim());
                c.setUsername(dto.getNombres().trim());
            }
            if (dto.getApellido() != null && !dto.getApellido().isBlank()) c.setApellido(dto.getApellido().trim());
            if (dto.getTipoDocumento() != null) c.setTipoDocumento(dto.getTipoDocumento());
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().isBlank()) {
                c.setNumeroDocumento(dto.getNumeroDocumento().trim());
            }
            if (dto.getGenero() != null) c.setGenero(dto.getGenero());
            if (dto.getEstadoCivil() != null) c.setEstadoCivil(dto.getEstadoCivil());
            if (dto.getFechaNacimiento() != null && !dto.getFechaNacimiento().isBlank()) {
                try {
                    c.setFechaNacimiento(java.time.LocalDate.parse(dto.getFechaNacimiento()));
                } catch (Exception e) {
                    // Ignorar error de fecha
                }
            }
            if (dto.getNacionalidad() != null) c.setNacionalidad(dto.getNacionalidad());
            if (dto.getTelefono() != null) {
                String tel = dto.getTelefono().replaceAll("[^0-9]", "");
                if (tel.length() > 10) tel = tel.substring(0, 10);
                c.setTelefono(tel);
            }
            if (dto.getTelefonoFijo() != null) c.setTelefonoFijo(dto.getTelefonoFijo());
            if (dto.getDireccion() != null) c.setDireccion(dto.getDireccion());
            if (dto.getSobreMi() != null) c.setSobreMi(dto.getSobreMi());
            if (dto.getTecnologias() != null) c.setTecnologias(dto.getTecnologias());
            if (dto.getIdiomas() != null) c.setIdiomas(dto.getIdiomas());
            if (dto.getCargo() != null) c.setCargo(dto.getCargo());
            if (dto.getAreaProfesional() != null) c.setAreaProfesional(dto.getAreaProfesional());
            if (dto.getPretensionSalarial() != null) c.setPretensionSalarial(dto.getPretensionSalarial());
            if (dto.getDisponibilidad() != null) c.setDisponibilidad(dto.getDisponibilidad());
            if (dto.getModalidadTrabajo() != null) c.setModalidadTrabajo(dto.getModalidadTrabajo());
            if (dto.getCiudad() != null) c.setCiudad(dto.getCiudad());
            if (dto.getFormacionJson() != null) c.setFormacionJson(dto.getFormacionJson());
            if (dto.getExperienciaJson() != null) c.setExperienciaJson(dto.getExperienciaJson());
            if (dto.getIdiomasJson() != null) c.setIdiomasJson(dto.getIdiomasJson());

            c.setUltimaActualizacion(LocalDateTime.now());
            candidatoService.guardar(c);

            resp.put("success", true);
            resp.put("message", "Perfil actualizado correctamente.");
            return ResponseEntity.ok(resp);
        }

        resp.put("success", false);
        resp.put("message", "Candidato no encontrado");
        return ResponseEntity.badRequest().body(resp);
    }
}
