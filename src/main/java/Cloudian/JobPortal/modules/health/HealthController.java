package Cloudian.JobPortal.modules.health;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("health")
@Tag(name = "Health", description = "System health check APIs (readiness, error testing)")
public class HealthController {
    @GetMapping
    @Operation(summary = "Health check", description = "Checks if the application is running. Returns a simple status message.")
    public ResponseEntity<?> checkingAppHealth() {
        return ResponseEntity.status(HttpStatus.OK).body("Your app is running. Build with Cloudian ❤\uFE0F Cloud");
    }
    @GetMapping("readness") 
    @Operation(summary = "Readiness check", description = "Checks if the application is ready to serve requests.")
    public ResponseEntity<?> checkingAppResponse() { 
        HealthResponse healthResponse = HealthResponse.builder().message("Hello World").success(true).build(); 
        return ResponseEntity.status(HttpStatus.OK).body(healthResponse); 
    }
    @GetMapping("error") 
    @Operation(summary = "Test error handling", description = "Throws a sample error for testing error handling. For development purposes.")
    public void checkingAppResponseError() 
    {
        throw new BadRequestException("Sample error message from server"); 
        
    }
}
