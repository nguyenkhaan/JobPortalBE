package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.Industry;
import Cloudian.JobPortal.models.JobIndustry;
import Cloudian.JobPortal.models.JobPost;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.industry.IndustryRepository;
import Cloudian.JobPortal.modules.jobindustry.JobIndustryRepository;
import Cloudian.JobPortal.modules.jobpost.dto.CreateJobPostDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobPostService {

    @Autowired
    private JobPostRepository jobPostRepository;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private IndustryRepository industryRepository;
    @Autowired
    private JobIndustryRepository jobIndustryRepository;
    public List<JobPost> getAllJobPost(JobPostFilterRequest filter, int limit, int offset) {

        // Validations
        if (limit <= 0) {
            throw new BadRequestException("Limit must be greater than 0");
        }
        if (offset < 0) {
            throw new BadRequestException("Offset/Page index cannot be less than 0");
        }
        Pageable pageable = PageRequest.of(offset, limit);

        Specification<JobPost> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.getKeyword().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getSalaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), filter.getSalaryMin()));
            }

            if (filter.getSalaryMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMax"), filter.getSalaryMax()));
            }

            if (filter.getEducationLevel() != null) {
                predicates.add(cb.equal(root.get("educationLevel"), filter.getEducationLevel()));
            }

            if (filter.getJobLevel() != null) {
                predicates.add(cb.equal(root.get("jobLevel"), filter.getJobLevel()));
            }

            if (filter.getIndustryIds() != null && !filter.getIndustryIds().isEmpty()) {  //filter by the industry list
                query.distinct(true); // Prevents row duplication from multi-matching joins
                Join<Object, Object> jobIndustryList = root.join("jobIndustryList");
                predicates.add(jobIndustryList.get("industry").get("id").in(filter.getIndustryIds()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return jobPostRepository.findAll(spec, pageable).getContent();
    }
    @Transactional
    public JobPost createJobPost(Long userId , CreateJobPostDto data) {
        EmployerProfile employer = employerRepository.findByOwnerId(userId).orElse(null);
        if (employer == null)
            throw new NotFoundException("User doesn't register an employer profile");
        JobPost savedJobPost = JobPost.builder()
                .title(data.getTitle())
                .description(data.getDescription())
                .jobLevel(data.getJobLevel())
                .experience(data.getExperience())
                .educationLevel(data.getEducationLevel())
                .status(data.getStatus())
                .employmentType(data.getEmploymentType())
                .build();

        List<JobIndustry> jobIndustryList = new ArrayList<>();
        for (Long industryId : data.getIndustryIds())
        {
            Industry industry = industryRepository.findById(industryId).orElse(null);
            if (industry == null) continue;
            jobIndustryList.add(
                    JobIndustry.builder().industry(industry).jobPost(savedJobPost).build()
            );
        }
        jobIndustryRepository.saveAll(jobIndustryList);
        jobPostRepository.save(savedJobPost);
        return savedJobPost;

    }
}