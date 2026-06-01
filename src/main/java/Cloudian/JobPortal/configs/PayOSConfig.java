package Cloudian.JobPortal.configs;
import vn.payos.PayOS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayOSConfig {

    @Value("${app.payos.client-id}")
    private String clientId;
    @Value("${app.payos.api-key}")
    private String apiKey;
    @Value("${app.payos.checksum-key}")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}