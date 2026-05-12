package br.gov.rn.natal.cadpgmapi.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MinioConfig {
    @Value("${minio.url}") private String url;
    @Value("${minio.public-url}") private String publicUrl;
    @Value("${minio.access-key}") private String accessKey;
    @Value("${minio.secret-key}") private String secretKey;
    @Value("${minio.bucket-name}") private String bucketName;

    // 1. CLIENTE INTERNO (Para Upload/Delete - Faz requisição de rede)
    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();

        // Passo 0: Inicialização Automática do Bucket
        try {
            boolean found = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar bucket no MinIO", e);
        }

        return client;
    }

    // 2. CLIENTE PÚBLICO (Exclusivo para gerar a URL com a assinatura matemática correta)
    @Bean("minioPublicClient")
    public MinioClient minioPublicClient() {
        return MinioClient.builder()
                .endpoint(publicUrl) // 🌟 Usa a URL do navegador!
                .credentials(accessKey, secretKey)
                .region("us-east-1")
                .build();
    }
}
