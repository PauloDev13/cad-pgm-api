package br.gov.rn.natal.cadpgmapi.load_pdf.services;

import br.gov.rn.natal.cadpgmapi.config.MinioConfig;
import io.minio.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@Service
public class DocumentoStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    public DocumentoStorageService(
            MinioClient minioClient, MinioConfig minioConfig
    ) {
        this.minioClient = minioClient;
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

        String internalURL =  minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .bucket(minioConfig.getBucketName())
                        .object(objectName)
                        .expiry(15, TimeUnit.MINUTES) // Link expira em 15 min por segurança
                        .build()
        );

        String configuredURL = minioConfig.getUrl();
        String publicURL = minioConfig.getPublicUrl();

        if (publicURL != null && !publicURL.isBlank() && !configuredURL.equals(publicURL)) {
            return internalURL.replace(configuredURL, publicURL);
        }

        return internalURL;
    }

    public void remove(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .build());
    }
}