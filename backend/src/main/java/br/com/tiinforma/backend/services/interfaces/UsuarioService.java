package br.com.tiinforma.backend.services.interfaces;


import br.com.tiinforma.backend.domain.usuario.UsuarioCreateDto;
import br.com.tiinforma.backend.domain.usuario.UsuarioResponseDto;

import java.util.List;

public interface UsuarioService {

    UsuarioResponseDto findById(Long id);

    List<UsuarioResponseDto> findAll();

    UsuarioResponseDto create(UsuarioCreateDto usuarioCreateDto);

    UsuarioCreateDto update(UsuarioCreateDto usuarioCreateDto);

    void delete(Long id);
}
