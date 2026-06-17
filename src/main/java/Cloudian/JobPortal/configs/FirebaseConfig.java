package Cloudian.JobPortal.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() throws IOException {

        GoogleCredentials credentials;

        String firebaseCredentialJson =
                System.getenv("FIREBASE_CREDENTIAL_JSON");

        if (firebaseCredentialJson != null &&
            !firebaseCredentialJson.isBlank()) {

            credentials = GoogleCredentials.fromStream(
                    new ByteArrayInputStream(
                            firebaseCredentialJson.getBytes(StandardCharsets.UTF_8)
                    )
            );

        } else {

            InputStream serviceAccount =
                    getClass()
                            .getClassLoader()
                            .getResourceAsStream("firebase-service-account.json");

            if (serviceAccount == null) {
                throw new IllegalStateException(
                        "Firebase credential not found."
                );
            }

            credentials = GoogleCredentials.fromStream(serviceAccount);
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}