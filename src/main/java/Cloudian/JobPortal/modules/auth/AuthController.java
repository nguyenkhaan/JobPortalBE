package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.modules.auth.dto.AuthLoginRequest;
import Cloudian.JobPortal.modules.auth.dto.AuthLoginResponse;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterRequest;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterResponse;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/verify")
    public ResponseEntity<?> verify(@Param("token") String token)
    {
        Boolean responseData = authService.authRegisterVerify(token);
        return ResponseEntity.status((responseData? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)).body(
                responseData
        );
    }
    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest data)
    {
        AuthLoginResponse responseData = authService.login(data);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
    @PreAuthorize("hasRole('SEEKER')")   //Them has Role vao phia truoc la kiem tra duoc role
    @GetMapping("testing-role")
    public String testingRole(Authentication authentication)
    {
        System.out.println(authentication.getName());
        System.out.println(authentication.getCredentials());
        return "Role Testing Successfully";
    }
    @PostMapping("reset-password")
    public ResponseEntity<?> resetPasswod(@RequestBody String email)
    {
        String responseData = authService.resetPassword();
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
}
