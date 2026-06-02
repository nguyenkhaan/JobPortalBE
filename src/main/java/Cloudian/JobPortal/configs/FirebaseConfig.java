package Cloudian.JobPortal.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void initialize() throws IOException {

        GoogleCredentials credentials = GoogleCredentials
                .fromStream(
                        getClass()
                                .getClassLoader()
                                .getResourceAsStream("firebase-service-account.json")
                );

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
