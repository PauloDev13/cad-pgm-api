package br.gov.rn.natal.cadpgmapi.load_pdf.services;

import br.gov.rn.natal.cadpgmapi.config.MinioConfig;
import io.minio.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void upload(MultipartFile file, String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .object(path)
                        .stream(file.getInputStream(), file.getSize(), - 1L)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    public String getDownloadUrl(String objectName) throws Exception {
        return  minioPublicClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .bucket(minioConfig.getBucketName())
                        .object(objectName)
                        .expiry(15, TimeUnit.MINUTES) // Link expira em 15 min por segurança
                        .build()
        );

    }

    public void remove(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .build());
    }
}