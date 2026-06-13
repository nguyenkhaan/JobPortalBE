package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.models.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobPostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Test
    void findRecentJobsForSeeker_ReturnsOnlyEligibleRecentJobs() {
        LocalDateTime now = LocalDateTime.now();

        EmployerProfile approvedEmployer = persistEmployer("approved@test.com", ApprovalStatus.APPROVED, true);
        EmployerProfile rejectedEmployer = persistEmployer("rejected@test.com", ApprovalStatus.REJECTED, true);
        EmployerProfile inactiveEmployer = persistEmployer("inactive@test.com", ApprovalStatus.APPROVED, false);

        JobPost eligible = persistJob("Eligible Job", approvedEmployer, JobPostStatus.OPEN, now.minusDays(2), now.plusDays(5));
        persistJob("Old Job", approvedEmployer, JobPostStatus.OPEN, now.minusDays(8), now.plusDays(5));
        persistJob("Closed Job", approvedEmployer, JobPostStatus.CLOSED, now.minusDays(1), now.plusDays(5));
        persistJob("Expired Job", approvedEmployer, JobPostStatus.OPEN, now.minusDays(1), now.minusHours(1));
        persistJob("Rejected Employer Job", rejectedEmployer, JobPostStatus.OPEN, now.minusDays(1), now.plusDays(5));
        persistJob("Inactive Employer Job", inactiveEmployer, JobPostStatus.OPEN, now.minusDays(1), now.plusDays(5));

        entityManager.flush();
        entityManager.clear();

        var result = jobPostRepository.findRecentJobsForSeeker(
                now.minusDays(7),
                now,
                PageRequest.of(0, 10)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(JobPost::getTitle).containsExactly("Eligible Job");
        assertThat(result.getContent().get(0).getId()).isEqualTo(eligible.getId());
    }

    private EmployerProfile persistEmployer(String email, ApprovalStatus approvalStatus, boolean active) {
        User owner = User.builder()
                .email(email)
                .password("encoded-password")
                .active(true)
                .build();
        entityManager.persist(owner);

        EmployerProfile employer = EmployerProfile.builder()
                .owner(owner)
                .companyName("Company " + email)
                .companyWebsite("https://example.com")
                .address("HCMC")
                .phone("0900000000")
                .email(email)
                .approvalStatus(approvalStatus)
                .active(active)
                .build();
        entityManager.persist(employer);
        return employer;
    }

    private JobPost persistJob(
            String title,
            EmployerProfile employer,
            JobPostStatus status,
            LocalDateTime createdAt,
            LocalDateTime expiresAt
    ) {
        JobPost jobPost = JobPost.builder()
                .employer(employer)
                .title(title)
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .status(status)
                .educationLevel(EducationLevel.BACHELOR)
                .experience(1)
                .jobLevel(JobLevel.JUNIOR)
                .salaryMin(BigDecimal.valueOf(10_000_000))
                .salaryMax(BigDecimal.valueOf(20_000_000))
                .salaryType(SalaryType.MONTHLY)
                .location("HCMC")
                .expiresAt(expiresAt)
                .build();
        entityManager.persist(jobPost);
        entityManager.flush();

        jobPost.setCreatedAt(createdAt);
        jobPost.setExpiresAt(expiresAt);
        entityManager.flush();

        return jobPost;
    }
}
