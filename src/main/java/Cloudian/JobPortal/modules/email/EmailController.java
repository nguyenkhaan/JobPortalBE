package Cloudian.JobPortal.modules.email;

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
public class EmailController {
    @Autowired
    EmailService emailService;
    @GetMapping
    public ResponseEntity<String> testSendEmail() throws MessagingException {
        Map<String, Object> variables = Map.of(
                "name", "Nguyen Kha",
                "resetUrl", "http://localhost:3000/reset?token=abc123"
        );
        emailService.sendEmail("nguyenkhaan2006@gmail.com" , "Testing email" , EmailTemplate.RESET_PASSWORD.getPath() , variables);
        return ResponseEntity.status(HttpStatus.OK).body("Your has been sent. Check your mail box");
    }
}
