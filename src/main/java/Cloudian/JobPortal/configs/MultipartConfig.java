package Cloudian.JobPortal.configs;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipartConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement(
                null,
                1000 * 1024 * 1024, // 100MB per file
                3000L * 1024 * 1024, // 300MB total request
                0
        );
    }
}
