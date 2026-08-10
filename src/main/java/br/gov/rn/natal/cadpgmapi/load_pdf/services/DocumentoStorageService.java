package br.gov.rn.natal.cadpgmapi.load_pdf.services;

import br.gov.rn.natal.cadpgmapi.config.MinioConfig;
import io.minio.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentoStorageService {

    private final MinioClient minioClient;
    private final MinioClient minioPublicClient;
    private final MinioConfig minioConfig;

    public DocumentoStorageService(
            @Qualifier("minioClient")
            MinioClient minioClient,

            @Qualifier("minioPublicClient")
            MinioClient minioPublicClient,
            MinioConfig minioConfig
    ) {
        this.minioClient = minioClient;
        this.minioPublicClient = minioPublicClient;
        this.minioConfig = minioConfig;
    }

    // DEFICIÊNCIA CORRIGIDA (b4.25/b5.1): a API do MinIO só lança "Exception" genérica
    // (checked), o que obrigava a marcar métodos, controllers e serviços com "throws Exception".
    // Agora as falhas do MinIO são traduzidas para IOException com mensagem descritiva,
    // e as assinaturas públicas ficam limpas e coerentes com o restante do projeto.
    public void upload(MultipartFile file, String path) throws IOException {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Falha ao gravar o arquivo no MinIO: " + path, e);
        }
    }

    public String getDownloadUrl(String objectName) throws IOException {
        try {
            return minioPublicClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES) // Link expira em 15 min por segurança
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Falha ao gerar URL pré-assinada do arquivo: " + objectName, e);
        }
    }

    public void remove(String objectName) throws IOException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new IOException("Falha ao remover o arquivo do MinIO: " + objectName, e);
        }
    }

    // Puxa o fluxo de dados (bytes) diretamente do MinIO
    public InputStream getDownloadStream(String objectName) throws IOException {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName()) // O mesmo bucket que já usam
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new IOException("Falha ao abrir o fluxo do arquivo no MinIO: " + objectName, e);
        }
    }
}