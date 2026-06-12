package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.OrganizationType;
import Cloudian.JobPortal.modules.employer.dto.EmployerDetailResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerFilterRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerResponse;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostService;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse;
import Cloudian.JobPortal.modules.minio.MinioService;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicEmployerService {

    private final EmployerRepository employerRepository;
    private final JobPostRepository jobPostRepository;
    private final MinioService minioService;

    private Pageable buildPageable(int limit, int offset) {
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }
        if (offset < 0) {
            offset = 0;
        }
        int page = offset / limit;
        return PageRequest.of(page, limit);
    }

    @Transactional
    public Page<EmployerResponse> getAllEmployers(EmployerFilterRequest filter, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);

        Specification<EmployerProfile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show APPROVED and ACTIVE employers
            predicates.add(cb.equal(root.get("approvalStatus"), ApprovalStatus.APPROVED));
            predicates.add(cb.equal(root.get("active"), true));

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("companyName")),
                        "%" + filter.getKeyword().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("address")),
                        "%" + filter.getLocation().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getCategory() != null && !filter.getCategory().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("industry")),
                        "%" + filter.getCategory().trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<EmployerProfile> employerPage = employerRepository.findAll(spec, pageable);

        // Batch-load openJobsCount: single query for all employer IDs on this page
        Map<Long, Long> openJobsMap = batchLoadOpenJobsCount(employerPage.getContent());

        return employerPage.map(employer -> toEmployerResponse(employer, openJobsMap));
    }

    @Transactional
    public EmployerDetailResponse getEmployerDetail(Long id) {
        EmployerProfile employer = employerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Employer not found"));

        // Only show if approved and active
        if (employer.getApprovalStatus() != ApprovalStatus.APPROVED || !Boolean.TRUE.equals(employer.getActive())) {
            throw new NotFoundException("Employer not found");
        }

        return toDetailResponse(employer);
    }

    private Map<Long, Long> batchLoadOpenJobsCount(List<EmployerProfile> employers) {
        if (employers == null || employers.isEmpty()) {
            return Map.of();
        }
        List<Long> employerIds = employers.stream()
                .map(EmployerProfile::getId)
                .toList();

        List<Object[]> results = jobPostRepository.countOpenJobsByEmployerIds(employerIds);
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private String getLogoUrl(EmployerProfile employer) {
        if (employer == null || employer.getLogo() == null) {
            return null;
        }
        return minioService.getFileUrl(employer.getLogo());
    }


    private EmployerResponse toEmployerResponse(EmployerProfile employer, Map<Long, Long> openJobsMap) {
        return EmployerResponse.builder()
                .id(String.valueOf(employer.getId()))
                .name(employer.getCompanyName())
                .logo(getLogoUrl(employer))
                .location(employer.getAddress())
                .openJobsCount(openJobsMap.getOrDefault(employer.getId(), 0L).intValue())
                .category(employer.getIndustry())
                .build();
    }

    private EmployerDetailResponse toDetailResponse(EmployerProfile employer) {
        String logoUrl = getLogoUrl(employer);

        // Build overview
        EmployerDetailResponse.EmployerOverview overview = EmployerDetailResponse.EmployerOverview.builder()
                .founded(employer.getFounded() != null ? String.valueOf(employer.getFounded().getYear()) : null)
                .orgType(getOrganizationTypeLabel(employer.getOrganizationType()))
                .teamSize(employer.getTeamSize())
                .industry(employer.getIndustry())
                .build();

        // Build contact
        EmployerDetailResponse.EmployerContact contact = EmployerDetailResponse.EmployerContact.builder()
                .website(employer.getCompanyWebsite())
                .phone(employer.getPhone())
                .email(employer.getEmail())
                .build();

        return EmployerDetailResponse.builder()
                .id(String.valueOf(employer.getId()))
                .name(employer.getCompanyName())
                .logo(logoUrl)
                .category(employer.getIndustry())
                .description(employer.getDescription())
                .benefits(splitTextToList(employer.getBenefits()))
                .vision(employer.getVision())
                .overview(overview)
                .contact(contact)
                .build();
    }

  
    private List<String> splitTextToList(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.split("\\n\\n"));
    }


    private String getOrganizationTypeLabel(OrganizationType type) {
        if (type == null) return null;
        return type.label;
    }
}