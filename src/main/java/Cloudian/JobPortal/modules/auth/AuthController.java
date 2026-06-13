package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.modules.auth.dto.*;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
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

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "APIs for user registration, login, password reset, and token management")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/register")
    @Operation(summary = "Register a new account", description = "Creates a new user account with email verification. Sends a verification email.")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterRequest data) throws MessagingException
    {
        AuthRegisterResponse responseData = authService.register(data);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
    @GetMapping("/verify")
    @Operation(summary = "Verify email", description = "Verifies the user's email address using the verification token.")
    public ResponseEntity<?> verify(@Param("token") String token)
    {
        Boolean responseData = authService.authRegisterVerify(token);
        return ResponseEntity.status((responseData? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)).body(
                responseData
        );
    }
    @PostMapping("login")
    @Operation(summary = "Login", description = "Authenticates a user and returns access/refresh tokens.")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest data)
    {
        AuthLoginResponse responseData = authService.login(data);
        return ResponseEntity.status(HttpStatus.OK).body(responseData);
    }
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the profile information of the currently authenticated user.")
    public ResponseEntity<?> getMe(Authentication authentication) {
        String email = authentication.getName();
        AuthMeResponse response = authService.getMe(email);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SEEKER')")   //Them has Role vao phia truoc la kiem tra duoc role
    @GetMapping("testing-role")
    @Operation(summary = "Test role access", description = "Tests that the user has the SEEKER role. For development purposes only.")
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
    @Operation(summary = "Request password reset", description = "Sends a password reset link to the specified email address.")
    public ResponseEntity<?> resetPassword(@RequestParam("email") String email)
    {
        ResetPasswordResponse response = authService.resetPassword(email);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping("verify-reset-password")
    @Operation(summary = "Verify password reset", description = "Verifies the reset token and updates the password.")
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
    @PostMapping("reset-email")
    @Operation(summary = "Change email", description = "Changes the user's email address. Requires current password verification.")
    public ResponseEntity<?> changeEmail(
            Authentication authentication,
            @Valid @RequestBody ResetEmailRequest data
    )
    {
//        System.out.println(authentication.getName());
        String email = authentication.getName();
        Boolean response = authService.resetEmail(email, data.getPassword() , data.getNewEmail());
        HashMap<String , Object> bod = new HashMap<>();
        bod.put("status" , response);
        if (response)
            bod.put("message" , "Reset email successfully");
        else bod.put("message" , "Cannot reset email");
        return ResponseEntity.status(HttpStatus.OK).body(bod);
    }
    @GetMapping("refresh")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token.")
    public ResponseEntity<?> getAccessToken(
            @Param("token") String token
    )
    {
        RefreshResponse response = authService.refreshAccessToken(token);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
