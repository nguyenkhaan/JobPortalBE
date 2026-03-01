package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.modules.auth.dto.AuthRegisterRequest;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterResponse;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterRequest data)
    {
        AuthRegisterResponse responseData = authService.register(data);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
