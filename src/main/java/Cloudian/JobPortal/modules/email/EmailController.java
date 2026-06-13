package Cloudian.JobPortal.modules.email;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("email")
@Tag(name = "Email", description = "Email service test API (for development purposes only)")
public class EmailController {
    @Autowired
    EmailService emailService;
    @GetMapping
    @Operation(summary = "Test send email", description = "Sends a test email to a hardcoded address. For development purposes only.")
    public ResponseEntity<String> testSendEmail() throws MessagingException {
        Map<String, Object> variables = Map.of(
                "name", "Nguyen Kha",
                "resetUrl", "http://localhost:3000/reset?token=abc123"
        );
        emailService.sendEmail("nguyenkhaan2006@gmail.com" , "Testing email" , EmailTemplate.RESET_PASSWORD.getPath() , variables);
        return ResponseEntity.status(HttpStatus.OK).body("Your has been sent. Check your mail box");
    }
}
