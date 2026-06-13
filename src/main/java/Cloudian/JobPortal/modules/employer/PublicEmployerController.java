package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerDetailResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerFilterRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerResponse;
import Cloudian.JobPortal.modules.jobpost.JobPostService;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employers")
@RequiredArgsConstructor
@Tag(name = "Employers (Public)", description = "Public APIs for viewing employer listings and employer job posts")
public class PublicEmployerController {

    private final PublicEmployerService publicEmployerService;
    private final JobPostService jobPostService;

    @GetMapping
    @Operation(summary = "Get all employers", description = "Returns a paginated list of employers with optional filters (keyword, industry, location, etc.).")
    public ResponseEntity<ApiResponse<PageResponse<EmployerResponse>>> getAllEmployers(
            @ModelAttribute EmployerFilterRequest filter,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        Page<EmployerResponse> page = publicEmployerService.getAllEmployers(filter, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employer detail", description = "Returns detailed information of a specific employer by ID.")
    public ResponseEntity<EmployerDetailResponse> getEmployerDetail(@PathVariable Long id) {
        EmployerDetailResponse detail = publicEmployerService.getEmployerDetail(id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{employerId}/jobs")
    @Operation(summary = "Get employer's job posts", description = "Returns a paginated list of public job posts published by a specific employer.")
    public ResponseEntity<ApiResponse<PageResponse<JobPostResponse>>> getJobsByEmployer(
            @PathVariable Long employerId,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        Page<JobPostResponse> page = jobPostService.getPublicJobsByEmployerId(employerId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }
}
