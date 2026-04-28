package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.modules.auth.dto.*;
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

import java.util.HashMap;
import java.util.Map;

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
//    @PostMapping("reset-password")
//    public ResponseEntity<?> resetPasswod(@RequestBody String email)
//    {
//        String responseData = authService.resetPassword();
//        return ResponseEntity.status(HttpStatus.OK).body(responseData);
//    }
    @GetMapping("reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("email") String email)
    {
        ResetPasswordResponse response = authService.resetPassword(email);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("verify-reset-password")
    public ResponseEntity<?> verifyResetPassword(@Valid @RequestBody VerifyResetPasswordRequest data)
    {
        boolean response = authService.verifyResetPasswordToken(data.getToken() , data.getPassword());
        //Kieu String, Object se linh hoat hon
        //ArrayList + HashMap -===== Array + dictionary = Array + Object trong JS va Python
        HashMap<String , Object> body = new HashMap<>();
        body.put("status" , response);
        if (response)
            body.put("message" , "Password has been reset successfully");
        else body.put("message" , "Password cannot be reset");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }


}
