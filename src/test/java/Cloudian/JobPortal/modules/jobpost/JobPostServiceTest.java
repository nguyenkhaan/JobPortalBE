package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.industry.IndustryRepository;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobindustry.JobIndustryRepository;
import Cloudian.JobPortal.modules.jobpost.dto.*;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPostServiceTest {

    @Mock private EmployerRepository employerRepository;
    @Mock private IndustryRepository industryRepository;
    @Mock private JobIndustryRepository jobIndustryRepository;
    @Mock private AuditService auditService;
    @Mock private MinioService minioService;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private JobPostService jobPostService;

    private EmployerProfile mockEmployer;
    private User mockOwner;
    private JobPost mockJobPost;
    private EmployerSubscription mockSubscription;
    private CreateJobPostDto createDto;

    @BeforeEach
    void setUp() {
        mockOwner = User.builder().id(1L).build();

        mockEmployer = EmployerProfile.builder()
                .id(1L)
                .owner(mockOwner)
                .companyName("Tech Corp")
                .companyWebsite("https://techcorp.com")
                .logo("logo.png")
                .address("HCMC")
                .email("contact@techcorp.com")
                .phone("0901234567")
                .industry("IT")
                .organizationType(OrganizationType.PRIVATE_COMPANY)
                .teamSize("50-100")
                .build();

        mockJobPost = JobPost.builder()
                .id(100L)
                .employer(mockEmployer)
                .title("Java Developer")
                .description("Desc\n\nParagraph 2\n\nParagraph 3")
                .requirements("Task 1\n\nTask 2")
                .employmentType(EmploymentType.FULL_TIME)
                .status(JobPostStatus.OPEN)
                .educationLevel(EducationLevel.BACHELOR)
                .experience(3)
                .jobLevel(JobLevel.SENIOR)
                .salaryMin(new BigDecimal("15000000"))
                .salaryMax(new BigDecimal("25000000"))
                .salaryType(SalaryType.MONTHLY)
                .tags("java,spring")
                .isFeatured(false)
                .isHighlighted(false)
                .vacancies(2)
                .createdAt(LocalDateTime.now().minusDays(5))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();

        Plan plan = Plan.builder().maxJobPostsPerMonth(5).build();
        mockSubscription = EmployerSubscription.builder()
                .id(1L)
                .employer(mockEmployer)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .plan(plan)
                .build();

        createDto = CreateJobPostDto.builder()
                .title("New Job")
                .description("Desc")
                .salaryMin(new BigDecimal("10000000"))
                .salaryMax(new BigDecimal("20000000"))
                .educationLevel(EducationLevel.BACHELOR)
                .jobLevel(JobLevel.JUNIOR)
                .status(JobPostStatus.OPEN)
                .experience(2)
                .employmentType(EmploymentType.FULL_TIME)
                .tags("java")
                .build();
    }

    // ==================== validatePostingRights ====================

    @Test
    void createJobPost_SubscriptionNotFound_ThrowsBadRequest() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("No information about the business"));
    }

    @Test
    void createJobPost_SubscriptionExpired_ThrowsBadRequest() {
        EmployerSubscription expiredSub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(expiredSub));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void createJobPost_QuotaExceeded_ThrowsBadRequest() {
        Plan freePlan = Plan.builder().maxJobPostsPerMonth(2).build();
        EmployerSubscription sub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().plusDays(10))
                .plan(freePlan)
                .build();
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(sub));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(2);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("maximum limit"));
    }

    // ==================== validateSalaries ====================

    @Test
    void createJobPost_SalaryMinNull_ThrowsBadRequest() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(mockSubscription));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        CreateJobPostDto dto = CreateJobPostDto.builder()
                .title("Test").description("Desc")
                .salaryMin(null).salaryMax(new BigDecimal("20000000"))
                .educationLevel(EducationLevel.BACHELOR)
                .jobLevel(JobLevel.JUNIOR)
                .status(JobPostStatus.OPEN)
                .experience(2)
                .employmentType(EmploymentType.FULL_TIME)
                .tags("test")
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, dto));
        assertTrue(ex.getMessage().contains("Salary min and max are required"));
    }

    @Test
    void createJobPost_SalaryMinGreaterThanMax_ThrowsBadRequest() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(mockSubscription));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        CreateJobPostDto dto = CreateJobPostDto.builder()
                .title("Test").description("Desc")
                .salaryMin(new BigDecimal("50000000"))
                .salaryMax(new BigDecimal("20000000"))
                .educationLevel(EducationLevel.BACHELOR)
                .jobLevel(JobLevel.JUNIOR)
                .status(JobPostStatus.OPEN)
                .experience(2)
                .employmentType(EmploymentType.FULL_TIME)
                .tags("test")
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, dto));
        assertTrue(ex.getMessage().contains("cannot be greater"));
    }

    @Test
    void createJobPost_EmployerNotFound_ThrowsNotFound() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> jobPostService.createJobPost(1L, createDto));
    }

    // ==================== createJobPost Happy Path ====================

    @Test
    void createJobPost_Success_ReturnsResponse() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(mockSubscription));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(mockJobPost);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);

        JobPostResponse response = jobPostService.createJobPost(1L, createDto);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Java Developer");
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
        verify(auditService, times(1)).createAuditLog(any());
    }

    // ==================== assertOwnerOrAdmin ====================

    @Test
    void updateJobPost_NotOwnerAndNotAdmin_ThrowsForbidden() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));

        UpdateJobPostDto updateDto = UpdateJobPostDto.builder()
                .title("Updated Title")
                .build();

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> jobPostService.updateJobPost(100L, 888L, false, updateDto));
        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void updateJobPost_AdminCanUpdate_OtherOwnerJob() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(mockJobPost);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        UpdateJobPostDto updateDto = UpdateJobPostDto.builder()
                .title("Admin Updated")
                .build();

        JobPostResponse response = jobPostService.updateJobPost(100L, 1L, true, updateDto);
        assertThat(response).isNotNull();
    }

    @Test
    void updateJobPost_EmptyIndustryIds_ThrowsBadRequest() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));

        UpdateJobPostDto updateDto = UpdateJobPostDto.builder()
                .industryIds(List.of())
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.updateJobPost(100L, 1L, false, updateDto));
        assertTrue(ex.getMessage().contains("At least one industry"));
    }

    // ==================== deleteJobPost ====================

    @Test
    void deleteJobPost_Success() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> jobPostService.deleteJobPost(100L, 1L, false));
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
        verify(auditService, times(1)).createAuditLog(any());
    }

    @Test
    void deleteJobPost_NotOwnerAndNotAdmin_ThrowsForbidden() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> jobPostService.deleteJobPost(100L, 888L, false));
        assertTrue(ex.getMessage().contains("permission"));
    }

    // ==================== getJobPostById ====================

    @Test
    void getJobPostById_Found_ReturnsDetailResponse() {
        when(jobPostRepository.findByIdWithEmployer(100L)).thenReturn(Optional.of(mockJobPost));
        when(minioService.getFileUrl(any())).thenReturn("https://minio/logo.png");

        JobPostDetailResponse response = jobPostService.getJobPostById(100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("100");
        assertThat(response.getTitle()).isEqualTo("Java Developer");
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(response.getType()).isEqualTo("Full Time");
        assertThat(response.getWebsite()).isEqualTo("https://techcorp.com");
        assertThat(response.getPhone()).isEqualTo("0901234567");
        assertThat(response.getEmail()).isEqualTo("contact@techcorp.com");
    }

    @Test
    void getJobPostById_NotFound_ThrowsNotFound() {
        when(jobPostRepository.findByIdWithEmployer(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> jobPostService.getJobPostById(999L));
    }

    // ==================== Helper methods via toDetailResponse ====================

    @Test
    void getJobPostById_OverviewMapping() {
        when(jobPostRepository.findByIdWithEmployer(100L)).thenReturn(Optional.of(mockJobPost));
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        JobPostDetailResponse response = jobPostService.getJobPostById(100L);

        assertThat(response.getOverview()).isNotNull();
        assertThat(response.getOverview().getSalary()).contains("15,000,000");
        assertThat(response.getOverview().getJobType()).isEqualTo("Full Time");
        assertThat(response.getOverview().getEducation()).isEqualTo("Đại học");
        assertThat(response.getOverview().getLocation()).isEqualTo("HCMC");
        assertThat(response.getOverview().getExperience()).isEqualTo("3 năm");
    }

    @Test
    void getJobPostById_CompanyProfileMapping() {
        when(jobPostRepository.findByIdWithEmployer(100L)).thenReturn(Optional.of(mockJobPost));
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        JobPostDetailResponse response = jobPostService.getJobPostById(100L);

        assertThat(response.getCompanyProfile()).isNotNull();
        assertThat(response.getCompanyProfile().getIndustry()).isEqualTo("IT");
        assertThat(response.getCompanyProfile().getOrgType()).isEqualTo("Private Company");
        assertThat(response.getCompanyProfile().getCompanySize()).isEqualTo("50-100");
    }

    @Test
    void getJobPostById_NullEmployer_NullSafeMapping() {
        mockJobPost.setEmployer(null);
        when(jobPostRepository.findByIdWithEmployer(100L)).thenReturn(Optional.of(mockJobPost));

        JobPostDetailResponse response = jobPostService.getJobPostById(100L);

        assertThat(response.getCompanyName()).isNull();
        assertThat(response.getPhone()).isNull();
        assertThat(response.getOverview().getLocation()).isNull();
        assertThat(response.getCompanyProfile().getIndustry()).isNull();
        assertThat(response.getCompanyProfile().getFoundedIn()).isNull();
        mockJobPost.setEmployer(mockEmployer);
    }

    // ==================== toResponse via getAllJobPost ====================

    @Test
    void getAllJobPost_WithFilter_ReturnsMappedPage() {
        JobPostFilterRequest filter = new JobPostFilterRequest();
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        when(jobPostRepository.findAll(anySpec, any(PageRequest.class))).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(filter, 10, 0);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Developer");
    }

    @Test
    void getAllJobPost_InvalidLimit_ThrowsBadRequest() {
        JobPostFilterRequest filter = new JobPostFilterRequest();
        assertThrows(BadRequestException.class,
                () -> jobPostService.getAllJobPost(filter, 0, 0));
        assertThrows(BadRequestException.class,
                () -> jobPostService.getAllJobPost(filter, 101, 0));
    }

    @Test
    void getAllJobPost_NegativeOffset_ThrowsBadRequest() {
        JobPostFilterRequest filter = new JobPostFilterRequest();
        assertThrows(BadRequestException.class,
                () -> jobPostService.getAllJobPost(filter, 10, -1));
    }

    // ==================== highlightJobPost ====================

    @Test
    void highlightJobPost_Success() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(mockSubscription));

        Plan paidPlan = Plan.builder().maxJobPostsPerMonth(5).price(100000.0).build();
        mockSubscription.setPlan(paidPlan);

        Map<String, Object> result = jobPostService.highlightJobPost(100L, 1L);

        assertThat(result.get("message")).isEqualTo("Job post highlighted successfully");
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
        assertTrue(mockJobPost.getIsHighlighted());
    }

    @Test
    void highlightJobPost_NotOwner_ThrowsForbidden() {
        when(employerRepository.findByOwnerId(888L)).thenReturn(Optional.of(mockEmployer));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> jobPostService.highlightJobPost(100L, 888L));
        assertTrue(ex.getMessage().contains("permission"));
    }

    @Test
    void highlightJobPost_SubscriptionExpired_ThrowsBadRequest() {
        EmployerSubscription expiredSub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(expiredSub));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.highlightJobPost(100L, 1L));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void highlightJobPost_FreePlan_ThrowsForbidden() {
        Plan freePlan = Plan.builder().maxJobPostsPerMonth(2).price(0.0).build();
        EmployerSubscription sub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().plusDays(10))
                .plan(freePlan)
                .build();
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(sub));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> jobPostService.highlightJobPost(100L, 1L));
        assertTrue(ex.getMessage().contains("Free plan"));
    }

    @Test
    void highlightJobPost_CooldownActive_ThrowsBadRequest() {
        mockJobPost.setPushedAt(LocalDateTime.now().minusHours(2));
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));

        Plan paidPlan = Plan.builder().maxJobPostsPerMonth(5).price(100000.0).build();
        EmployerSubscription sub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().plusDays(10))
                .plan(paidPlan)
                .build();
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(sub));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.highlightJobPost(100L, 1L));
        assertTrue(ex.getMessage().contains("thử lại sau"));
    }

    // ==================== getEmployerJobPosts ====================

    @Test
    void getEmployerJobPosts_Success() {
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findByEmployer_Owner_Id(eq(1L), any())).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getEmployerJobPosts(1L, 10, 0);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Developer");
    }

    // ==================== Salary formatting in toResponse ====================

    @Test
    void toResponse_SalaryFormattedCorrectly() {
        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findAll(anySpec, any(PageRequest.class))).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        JobPostResponse resp = result.getContent().get(0);

        assertThat(resp.getSalary()).contains("15,000,000");
        assertThat(resp.getSalary()).contains("25,000,000");
        assertThat(resp.getSalary()).contains("monthly");
    }

    @Test
    void toResponse_NullSalary_ThoaThuan() {
        mockJobPost.setSalaryMin(null);
        mockJobPost.setSalaryMax(null);

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findAll(anySpec, any(PageRequest.class))).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        assertThat(result.getContent().get(0).getSalary()).isEqualTo("Thỏa thuận");

        mockJobPost.setSalaryMin(new BigDecimal("15000000"));
        mockJobPost.setSalaryMax(new BigDecimal("25000000"));
    }

    // ==================== Experience & Days remaining ====================

    @Test
    void toResponse_NullExperience_ReturnsKhongYeuCau() {
        mockJobPost.setExperience(null);

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findAll(anySpec, any(PageRequest.class))).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        assertThat(result.getContent().get(0).getExperienceLabel()).isEqualTo("Không yêu cầu");

        mockJobPost.setExperience(3);
    }

    @Test
    void toResponse_DaysRemainingCalculated() {
        mockJobPost.setExpiresAt(LocalDateTime.now().plusDays(5));

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findAll(anySpec, any(PageRequest.class))).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        assertThat(result.getContent().get(0).getDaysRemaining()).contains("Còn");

        mockJobPost.setExpiresAt(LocalDateTime.now().plusDays(10));
    }

    @Test
    void toResponse_NullExpiresAt_VoThoiHan() {
        mockJobPost.setExpiresAt(null);

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findAll(anySpec, any(PageRequest.class))).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        assertThat(result.getContent().get(0).getDaysRemaining()).isEqualTo("Vô thời hạn");

        mockJobPost.setExpiresAt(LocalDateTime.now().plusDays(10));
    }
}