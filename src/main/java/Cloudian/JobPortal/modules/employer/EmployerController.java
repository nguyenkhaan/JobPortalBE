package Cloudian.JobPortal.modules.employer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("employer")
public class EmployerController {
    @PostMapping
    public ResponseEntity<?> registerEmployerProfile()
    {
        return ResponseEntity.status(200).body("message");
    }
}
