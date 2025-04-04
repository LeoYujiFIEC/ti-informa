package br.com.tiinforma.backend.domain.video;


import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record VideoCreateDto(
        Long idCriador,

        @NotBlank(message = "O video nescessita de um titulo")
        String titulo,

        @NotBlank(message = "O video nescessita de um descrição")
        String descricao,
        @URL
        String url,

        String categoria,

        List<String> palavrasChave
) {
}
