package com.back.controller;

import com.back.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Endpoint para eliminar un usuario por correo electrónico
     * Uso: POST /admin/eliminar-usuario?correo=usuario@example.com
     */
    @PostMapping("/eliminar-usuario")
    @ResponseBody
    public String eliminarUsuario(@RequestParam String correo) {
        try {
            usuarioRepository.findByCorreo(correo).ifPresentOrElse(
                usuario -> {
                    usuarioRepository.delete(usuario);
                    logger.info("Usuario eliminado: {}", correo);
                },
                () -> {
                    logger.warn("Usuario no encontrado: {}", correo);
                }
            );
            return "Usuario eliminado exitosamente";
        } catch (Exception e) {
            logger.error("Error al eliminar usuario: {}", e.getMessage());
            return "Error al eliminar usuario: " + e.getMessage();
        }
    }

    /**
     * Endpoint para eliminar todos los usuarios
     * ADVERTENCIA: Esto eliminará todos los usuarios de la base de datos
     * Uso: POST /admin/limpiar-base-datos
     */
    @PostMapping("/limpiar-base-datos")
    @ResponseBody
    public String limpiarBaseDatos() {
        try {
            long totalUsuarios = usuarioRepository.count();
            usuarioRepository.deleteAll();
            logger.info("Base de datos limpiada. {} usuarios eliminados", totalUsuarios);
            return "Base de datos limpiada. " + totalUsuarios + " usuarios eliminados";
        } catch (Exception e) {
            logger.error(" Error al limpiar base de datos: {}", e.getMessage());
            return "Error al limpiar base de datos: " + e.getMessage();
        }
    }
}
