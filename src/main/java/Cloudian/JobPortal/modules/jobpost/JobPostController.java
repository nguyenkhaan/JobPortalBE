package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.JobPost;
import Cloudian.JobPortal.modules.jobpost.dto.CreateJobPostDto;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController()
@RequestMapping("jobpost")
public class JobPostController {
    @Autowired
    JobPostService jobPostService;
    @GetMapping()
    public ResponseEntity<?> getAllJobPost(
            @ModelAttribute JobPostFilterRequest request,
            @RequestParam(required = false , defaultValue = "0") Integer offset,
            @RequestParam(required = false , defaultValue = "20") Integer limit
    )
    {
        List<JobPost> response = jobPostService.getAllJobPost(request , limit , offset);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    @PostMapping()
    public ResponseEntity<?> createJobPost(@RequestBody() @Valid  CreateJobPostDto data,
                                           Authentication authentication)
    {
        UserDetailsImpl user = (UserDetailsImpl)authentication.getPrincipal();
        if (user == null)
            throw new BadRequestException("Invalid user information");
        JobPost response = jobPostService.createJobPost(user.getId() , data);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully");
    }
}
//enum.name() = String