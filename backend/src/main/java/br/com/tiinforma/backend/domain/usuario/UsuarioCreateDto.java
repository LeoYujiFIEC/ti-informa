package br.com.tiinforma.backend.domain.usuario;

import br.com.tiinforma.backend.domain.assinatura.AssinaturaResponseDto;
import br.com.tiinforma.backend.domain.playlist.PlaylistResponseDto;
import br.com.tiinforma.backend.domain.usuario.UsuarioResponseDto;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioCreateDto {
    private Long id;
    private String nome;
    private String email;

    @Size(min = 8, message = "A senha deve conter no mínimo 8 caracteres")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
            message = "A senha deve ter pelo menos 1 letra maiúscula, 1 letra minúscula, 1 número e 1 caractere especial"
    )
    private String senha;
    private String interesses;
    private String descricao;
    private List<PlaylistResponseDto> playlistDtos;
    private List<AssinaturaResponseDto> assinaturaDtos;
    private List<UsuarioResponseDto> pergunta_resposta;
}