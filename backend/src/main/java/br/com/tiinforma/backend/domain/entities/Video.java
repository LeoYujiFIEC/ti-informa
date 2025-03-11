package br.com.tiinforma.backend.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    @Column(unique = true)
    private String url;

    private LocalDate dataPublicacao = LocalDate.now();

    private String categoria;

    private String palavraChave;

    @ManyToOne
    @JoinColumn(name = "id_criador")
    private Criador criador;

    @OneToMany(mappedBy = "video")
    private List<PlaylistVideo> playlistVideos;

    @OneToMany(mappedBy = "video")
    private List<UsuarioVideoProgresso> progressos;
}
