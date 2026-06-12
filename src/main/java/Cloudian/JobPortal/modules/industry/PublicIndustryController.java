package Cloudian.JobPortal.modules.industry;

import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/industries")
@RequiredArgsConstructor
public class PublicIndustryController {

    private final IndustryService industryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<IndustryResponse>>> getAllIndustries() {
        // Get all non-deleted industries with pagination: use large offset/limit to get all
        var page = industryService.getAllIndustry(null, 0, 100);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent()));
    }
}