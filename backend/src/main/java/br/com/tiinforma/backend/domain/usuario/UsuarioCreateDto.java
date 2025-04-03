package br.com.tiinforma.backend.domain.usuario;

import br.com.tiinforma.backend.domain.enums.Funcao;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


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
    private Funcao funcao;
}
