package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileUpdateRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerSubscriptionResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("employer")
public class EmployerController {
    @Autowired
    EmployerService employerService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployerProfileResponse> createEmployerProfile(
            @ModelAttribute @Valid CreateEmployerProfileRequest data,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        EmployerProfileResponse emp = employerService.createEmployer(data, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(emp);
    }

    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getEmployerProfile(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.getEmployerProfile(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/subscription")
    public ResponseEntity<EmployerSubscriptionResponse> getEmployerSubscription(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerSubscriptionResponse response = employerService.getEmployerSubscription(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping
    public ResponseEntity<EmployerProfileResponse> updateEmployerProfile(
            @Valid @ModelAttribute EmployerProfileUpdateRequest data,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.updateEmployerProfile(user.getId(), data);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
