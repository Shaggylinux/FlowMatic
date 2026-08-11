package com.back.candidatos;

import com.back.auth.Usuario;
import com.back.auth.UsuarioRepository;
import com.back.notificaciones.Notificacion;
import com.back.notificaciones.NotificacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class PerfilCandidatoController {

    private final UsuarioRepository usuarioRepository;
    private final CandidatoRepository candidatoRepository;
    private final NotificacionRepository notificacionRepository;

    public PerfilCandidatoController(UsuarioRepository usuarioRepository,
                                     CandidatoRepository candidatoRepository,
                                     NotificacionRepository notificacionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.candidatoRepository = candidatoRepository;
        this.notificacionRepository = notificacionRepository;
    }

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
            Optional<Usuario> uOpt = usuarioRepository.findByEmail(email);
            if (uOpt.isPresent()) {
                Usuario u = uOpt.get();
                candidatoId = u.getId();
                Optional<Candidato> cOpt = candidatoRepository.findById(u.getId());
                if (cOpt.isPresent()) {
                    Candidato c = cOpt.get();
                    if (c.getNombres() != null && !c.getNombres().isBlank()) {
                        nombres = c.getNombres();
                    } else if (c.getUsername() != null && !c.getUsername().isBlank()) {
                        nombres = c.getUsername();
                    }

                    if (c.getApellido() != null && !c.getApellido().isBlank()) {
                        apellidos = c.getApellido();
                    }

                    if (nombres.isBlank()) {
                        nombres = email.split("@")[0];
                    }

                    primerNombre = nombres.split(" ")[0];
                    nombreCompleto = (nombres + " " + apellidos).trim();
                    
                    String n1 = !nombres.isEmpty() ? nombres.substring(0, 1).toUpperCase() : "C";
                    String a1 = !apellidos.isEmpty() ? apellidos.substring(0, 1).toUpperCase() : "";
                    iniciales = n1 + a1;

                    if (c.getEstado() != null && !c.getEstado().isBlank()) estado = c.getEstado();
                    if (c.getCargo() != null) cargo = c.getCargo();
                    if (c.getCiudad() != null) ciudad = c.getCiudad();
                    if (c.getTelefono() != null) telefono = c.getTelefono();
                    if (c.getTelefonoFijo() != null) telefonoFijo = c.getTelefonoFijo();
                    if (c.getDireccion() != null) direccion = c.getDireccion();
                    if (c.getTipoDocumento() != null && !c.getTipoDocumento().isBlank()) tipoDocumento = c.getTipoDocumento();
                    if (c.getNumeroDocumento() != null) numeroDocumento = c.getNumeroDocumento();
                    if (c.getGenero() != null && !c.getGenero().isBlank()) genero = c.getGenero();
                    if (c.getEstadoCivil() != null && !c.getEstadoCivil().isBlank()) estadoCivil = c.getEstadoCivil();
                    if (c.getFechaNacimiento() != null) {
                        fechaNacimientoStr = c.getFechaNacimiento().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.of("es", "ES")));
                        fechaNacimientoIso = c.getFechaNacimiento().toString();
                    }
                    if (c.getNacionalidad() != null && !c.getNacionalidad().isBlank()) nacionalidad = c.getNacionalidad();
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
        List<Notificacion> notificaciones = notificacionRepository.findTop5ByOrderByFechaDesc();
        long notificacionesNoLeidas = notificacionRepository.countByLeidaFalse();

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

        Optional<Usuario> uOpt = usuarioRepository.findByEmail(email);
        if (uOpt.isPresent()) {
            Usuario u = uOpt.get();
            Candidato c = candidatoRepository.findById(u.getId()).orElseGet(() -> {
                Candidato nuevo = new Candidato();
                nuevo.setId(u.getId());
                nuevo.setUsername(dto.getNombres() != null ? dto.getNombres() : u.getEmail());
                nuevo.setApellido(dto.getApellido() != null ? dto.getApellido() : "");
                return nuevo;
            });

            if (dto.getNombres() != null) {
                c.setNombres(dto.getNombres());
                c.setUsername(dto.getNombres());
            }
            if (dto.getApellido() != null) c.setApellido(dto.getApellido());
            if (dto.getTipoDocumento() != null) c.setTipoDocumento(dto.getTipoDocumento());
            if (dto.getNumeroDocumento() != null) c.setNumeroDocumento(dto.getNumeroDocumento());
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
            if (dto.getTelefono() != null) c.setTelefono(dto.getTelefono());
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
            candidatoRepository.save(c);

            resp.put("success", true);
            resp.put("message", "Perfil actualizado correctamente.");
            return ResponseEntity.ok(resp);
        }

        resp.put("success", false);
        resp.put("message", "Candidato no encontrado");
        return ResponseEntity.badRequest().body(resp);
    }
}
