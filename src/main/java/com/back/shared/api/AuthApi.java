package com.back.shared.api;

import com.back.shared.dto.RegistroUsuarioDTO;

public interface AuthApi {
    void registrarUsuario(RegistroUsuarioDTO dto);
}
