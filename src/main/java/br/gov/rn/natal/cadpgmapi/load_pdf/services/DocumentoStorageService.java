package br.gov.rn.natal.cadpgmapi.load_pdf.services;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
public class DocumentoStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public DocumentoStorageService(
            MinioClient minioClient,
            @Value("${minio.bucket-name}") String bucketName
    ) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public void upload(MultipartFile file, String path) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(file.getInputStream(), file.getSize(), - 1L)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    public String getDownloadUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(15, TimeUnit.MINUTES) // Link expira em 15 min por segurança
                        .build()
        );
    }

    public void remove(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
    }
}