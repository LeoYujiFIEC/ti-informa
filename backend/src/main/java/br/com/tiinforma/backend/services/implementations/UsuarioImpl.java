package br.com.tiinforma.backend.services.implementations;

import br.com.tiinforma.backend.domain.usuario.Usuario;
import br.com.tiinforma.backend.domain.usuario.UsuarioCreateDto;
import br.com.tiinforma.backend.domain.usuario.UsuarioResponseDto;
import br.com.tiinforma.backend.exceptions.ResourceNotFoundException;
import br.com.tiinforma.backend.repositories.UsuarioRepository;
import br.com.tiinforma.backend.services.interfaces.UsuarioService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UsuarioResponseDto findById(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        return modelMapper.map(usuario, UsuarioResponseDto.class);
    }

    @Override
    public List<UsuarioResponseDto> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDto create(UsuarioCreateDto usuarioCreateDto) {
        var entity = modelMapper.map(usuarioCreateDto, Usuario.class);
        entity = usuarioRepository.save(entity);
        return modelMapper.map(entity, UsuarioResponseDto.class);
    }

    @Override
    public UsuarioResponseDto update(Long id, UsuarioCreateDto usuarioCreateDto) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        usuario.setNome(usuarioCreateDto.getNome());
        usuario.setEmail(usuarioCreateDto.getEmail());
        usuario.setSenha(usuarioCreateDto.getSenha());
        usuario.setInteresses(usuarioCreateDto.getInteresses());

        var usuarioAtualizado = usuarioRepository.save(usuario);
        return modelMapper.map(usuarioAtualizado, UsuarioResponseDto.class);
    }

    @Override
    public void delete(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuarioRepository.delete(usuario);
    }
}
