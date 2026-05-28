package Cloudian.JobPortal.configs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MinioConfig {
//    @Bean
    @Value("${minio.access.name}")
    String accessKey;
    @Value("${minio.access.secret}")
    String secretKey;
    @Value("${minio.url}")
    String endpoint;
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
