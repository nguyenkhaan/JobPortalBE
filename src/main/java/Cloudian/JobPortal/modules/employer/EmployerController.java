package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileUpdateRequest;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("employer")
public class EmployerController {
    @Autowired EmployerService employerService;
    @PostMapping
    public ResponseEntity<?> createEmployerProfile(
            @ModelAttribute() @Valid  CreateEmployerProfileRequest data,
            @RequestParam(value = "logo" , required = false) MultipartFile file,
            Authentication authentication
    ){
        System.out.println("Da vao route");
        UserDetailsImpl user = (UserDetailsImpl)authentication.getPrincipal();
        System.out.println(user.getId());
        if (user == null)
            throw new UnauthorizedException("User not found");
        EmployerProfileResponse emp = employerService.createEmployer(data , user.getId() , file);
        return ResponseEntity.status(HttpStatus.CREATED).body(emp);
    }
    @GetMapping
    public ResponseEntity<?> getEmployerProfile(
            Authentication authentication
    )
    {
        UserDetailsImpl user = (UserDetailsImpl)authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.getEmployerProfile(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PatchMapping
    public ResponseEntity<?> updateEmployerProfile(
            @Valid @ModelAttribute EmployerProfileUpdateRequest data,
            Authentication authentication,
            @RequestParam(value = "logo" , required = false) MultipartFile file
    )
    {
        UserDetailsImpl user = (UserDetailsImpl)authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.updateEmployerProfile(user.getId(), data , file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
