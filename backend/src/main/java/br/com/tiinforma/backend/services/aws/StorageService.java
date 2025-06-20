package br.com.tiinforma.backend.services.aws;

import br.com.tiinforma.backend.domain.criador.Criador;
import br.com.tiinforma.backend.domain.enums.Funcao;
import br.com.tiinforma.backend.domain.usuario.Usuario;
import br.com.tiinforma.backend.domain.video.Video;
import br.com.tiinforma.backend.repositories.UsuarioRepository;
import br.com.tiinforma.backend.repositories.VideoRepository;
import br.com.tiinforma.backend.services.interfaces.FotoAtualizavel;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StorageService  {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public String uploadFile(
            MultipartFile file,
            MultipartFile thumbnail,
            String titulo,
            String descricao,
            String categoria,
            LocalDate dataCadastro,
            String palavraChaveString,
            Criador criador
    ) {
        if (file.isEmpty() || thumbnail.isEmpty()) {
            throw new IllegalArgumentException("O arquivo e a thumbnail não podem estar vazios");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Tipo de arquivo inválido. Por favor, envie um vídeo.");
        }

        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        File thumbnailFile = convertMultiPartFileToFile(thumbnail);
        String thumbnailFileName = "thumb_" + System.currentTimeMillis() + "_" + thumbnail.getOriginalFilename();

        try {
            ObjectMetadata videoMetadata = new ObjectMetadata();
            videoMetadata.setContentType("video/mp4");
            PutObjectRequest videoPutRequest = new PutObjectRequest(bucketName, fileName, fileObj);
            videoPutRequest.setMetadata(videoMetadata);
            s3Client.putObject(videoPutRequest);

            ObjectMetadata thumbnailMetadata = new ObjectMetadata();
            thumbnailMetadata.setContentType(thumbnail.getContentType());
            s3Client.putObject(new PutObjectRequest(bucketName, thumbnailFileName, thumbnailFile));

            String thumbnailUrl = thumbnailFileName;

            List<String> palavrasChaveList = null;
            if (palavraChaveString != null && !palavraChaveString.trim().isEmpty()) {
                palavrasChaveList = Arrays.stream(palavraChaveString.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }

            log.info("Salvando vídeo com palavras-chave: {}", palavrasChaveList);
            Video video = Video.builder()
                    .titulo(titulo)
                    .descricao(descricao)
                    .categoria(categoria)
                    .palavraChave(palavrasChaveList != null ? String.join(",", palavrasChaveList) : null)
                    .dataPublicacao(dataCadastro != null ? dataCadastro : LocalDate.now())
                    .key(fileName)
                    .thumbnail(thumbnailUrl)
                    .criador(criador)
                    .build();
            log.info("Video a ser salvo: {}", video);

            videoRepository.save(video);

            return "File uploaded and video saved with key: " + fileName;
        } finally {
            if (fileObj.exists()) {
                fileObj.delete();
            }
            if (thumbnailFile.exists()) {
                thumbnailFile.delete();
            }
        }
    }

    public <T extends FotoAtualizavel> String uploadFoto(
            MultipartFile file,
            Long id,
            JpaRepository<T, Long> repository
    ){
        File fileObj = convertMultiPartFileToFile(file);
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();

        String fileUrl = s3Client.getUrl(bucketName, fileName).toString();

        T entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entidade não encontrada"));

        entity.setFotoUrl(fileUrl);
        repository.save(entity);

        return "Foto enviada e entidade atualizada com URL: " + fileUrl;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName,fileName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        try {
            byte[] content = IOUtils.toByteArray(inputStream);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Transactional
    public String deleteFile(String fileName, String username) {
        Video video = videoRepository.findByKey(fileName);

        if (video == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vídeo não encontrado");
        }

        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado"));

        boolean isAdmin = usuario.getFuncao() == Funcao.ADMINISTRADOR;

        if (!video.getCriador().getEmail().equals(username) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este vídeo");
        }

        try {
            s3Client.deleteObject(bucketName, fileName);

            if (video.getThumbnail() != null && !video.getThumbnail().isEmpty()) {
                s3Client.deleteObject(bucketName, video.getThumbnail());
            }

            videoRepository.delete(video);

            return "Vídeo excluído com sucesso: " + fileName;
        } catch (Exception e) {
            log.error("Erro ao excluir vídeo", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao excluir vídeo");
        }
    }

    private File convertMultiPartFileToFile(MultipartFile file){
        File convertFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertFile)){
            fos.write(file.getBytes());
        }
        catch (IOException e){
            log.error("Erro ao converter multiplos arquivos" + e);
        }
        return convertFile;
    }
}
