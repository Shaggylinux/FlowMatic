package com.back.shared.api;

import com.back.shared.dto.RegistroUsuarioDTO;

public interface AuthApi {
    String registrarUsuario(RegistroUsuarioDTO dto);
}
