package Cloudian.JobPortal.modules.industry;

import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/industries")
@RequiredArgsConstructor
@Tag(name = "Industries", description = "Public APIs for viewing industry categories")
public class PublicIndustryController {

    private final IndustryService industryService;

    @GetMapping
    @Operation(summary = "Get all industries", description = "Returns a list of all active industry categories available in the system.")
    public ResponseEntity<ApiResponse<List<IndustryResponse>>> getAllIndustries() {
        // Get all non-deleted industries with pagination: use large offset/limit to get all
        var page = industryService.getAllIndustry(null, 0, 100);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent()));
    }
}