package com.back.auth;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends CrudRepository<Token, String> {
    List<Token> findByUsuarioId(Long usuarioId);
}
