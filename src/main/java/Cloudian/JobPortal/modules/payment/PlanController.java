package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.Plan;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.payment.dto.PlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@Tag(name = "Plans", description = "The API gets a list of service packs")
public class PlanController {
    private final PlanRepository planRepository;

    @GetMapping
    @Operation(summary = "Get a list of all plans", description = "Returns the price, duration, and post limit configuration of each package, including new benefit fields: allowHighlight, featureDurationDays.")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getAllPlans() {
        List<Plan> plans = planRepository.findAll();
        List<PlanResponse> response = plans.stream()
                .map(PlanResponse::fromPlan)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Fetched plans successfully", response));
    }
}
