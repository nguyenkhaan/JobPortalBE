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
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials(accessKey, secretKey)
                .build();
    }
}
