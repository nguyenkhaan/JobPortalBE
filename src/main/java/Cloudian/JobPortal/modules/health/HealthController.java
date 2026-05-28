package Cloudian.JobPortal.modules.health;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("health")
public class HealthController {
    @GetMapping
    public ResponseEntity<?> checkingAppHealth() {
        return ResponseEntity.status(HttpStatus.OK).body("Your app is running. Build with Cloudian ❤\uFE0F Cloud");
    }
}
