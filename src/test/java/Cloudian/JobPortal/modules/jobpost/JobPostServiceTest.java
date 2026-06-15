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
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
                .salaryMin(new BigDecimal("1500"))
                .salaryMax(new BigDecimal("2500"))
                .salaryType(SalaryType.MONTHLY)
                .tags(List.of("java", "spring"))
                .isFeatured(true)
                .isHighlighted(true)
                .featureActivatedAt(LocalDateTime.now().minusDays(1))
                .featureExpiresAt(LocalDateTime.now().plusDays(4))
                .vacancies(2)
                .createdAt(LocalDateTime.now().minusDays(5))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .build();

        // Plan mặc định: Standard (allowHighlight=true, featureDurationDays=5)
        Plan plan = Plan.builder()
                .maxJobPostsPerMonth(5)
                .allowHighlight(true)
                .featureDurationDays(5)
                .build();

        mockSubscription = EmployerSubscription.builder()
                .id(1L)
                .employer(mockEmployer)
                .subStatus("ACTIVE")
                .startedAt(LocalDateTime.now().minusDays(30))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .plan(plan)
                .build();

        createDto = CreateJobPostDto.builder()
                .title("New Job")
                .description("Desc")
                .salaryMin(new BigDecimal("1000"))
                .salaryMax(new BigDecimal("2000"))
                .educationLevel(EducationLevel.BACHELOR)
                .jobLevel(JobLevel.JUNIOR)
                .status(JobPostStatus.OPEN)
                .experience(2)
                .employmentType(EmploymentType.FULL_TIME)
                .tags(List.of("java"))
                .build();
    }

    // ==================== validatePostingRights ====================

    @Test
    void createJobPost_SubscriptionNotFound_ThrowsBadRequest() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.empty());

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("No active subscription"));
    }

    @Test
    void createJobPost_SubscriptionExpired_ThrowsBadRequest() {
        EmployerSubscription expiredSub = EmployerSubscription.builder()
                .subStatus("ACTIVE")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .plan(Plan.builder().maxJobPostsPerMonth(5).build())
                .build();
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(expiredSub));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void createJobPost_QuotaExceeded_ThrowsBadRequest() {
        Plan freePlan = Plan.builder().maxJobPostsPerMonth(2).build();
        EmployerSubscription sub = EmployerSubscription.builder()
                .subStatus("ACTIVE")
                .startedAt(LocalDateTime.now().minusDays(30))
                .expiresAt(LocalDateTime.now().plusDays(10))
                .plan(freePlan)
                .build();
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(sub));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(2);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("maximum limit"));
    }

    // ==================== validateSalaries ====================

    @Test
    void createJobPost_SalaryMinNull_ThrowsBadRequest() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(mockSubscription));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        CreateJobPostDto dto = CreateJobPostDto.builder()
                .title("Test").description("Desc")
                .salaryMin(null).salaryMax(new BigDecimal("2000"))
                .educationLevel(EducationLevel.BACHELOR)
                .jobLevel(JobLevel.JUNIOR)
                .status(JobPostStatus.OPEN)
                .experience(2)
                .employmentType(EmploymentType.FULL_TIME)
                .tags(List.of("test"))
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, dto));
        assertTrue(ex.getMessage().contains("Salary min and max are required"));
    }

    @Test
    void createJobPost_SalaryMinGreaterThanMax_ThrowsBadRequest() {
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(mockSubscription));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        CreateJobPostDto dto = CreateJobPostDto.builder()
                .title("Test").description("Desc")
                .salaryMin(new BigDecimal("5000"))
                .salaryMax(new BigDecimal("2000"))
                .educationLevel(EducationLevel.BACHELOR)
                .jobLevel(JobLevel.JUNIOR)
                .status(JobPostStatus.OPEN)
                .experience(2)
                .employmentType(EmploymentType.FULL_TIME)
                .tags(List.of("test"))
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
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(mockSubscription));
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

    // ==================== createJobPost Auto-Apply ====================

    @Test
    void createJobPost_StandardPlan_AutoAppliesFeatureAndHighlight() {
        // Standard plan: allowHighlight=true, featureDurationDays=5
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(mockSubscription));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        when(jobPostRepository.save(any(JobPost.class))).thenAnswer(invocation -> {
            JobPost saved = invocation.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        JobPostResponse response = jobPostService.createJobPost(1L, createDto);

        // Verify auto-feature: featureDurationDays > 0 → isFeatured=true
        assertThat(response.getIsFeatured()).isTrue();
        assertThat(response.getFeatureActivatedAt()).isNotNull();
        assertThat(response.getFeatureExpiresAt()).isNotNull();

        // Verify auto-highlight: allowHighlight=true → isHighlighted=true
        assertThat(response.getIsHighlighted()).isTrue();
    }

    @Test
    void createJobPost_BasicPlan_AutoAppliesFeatureOnly() {
        // Basic plan: allowHighlight=false, featureDurationDays=3
        Plan basicPlan = Plan.builder()
                .maxJobPostsPerMonth(5)
                .allowHighlight(false)
                .featureDurationDays(3)
                .build();
        EmployerSubscription basicSub = EmployerSubscription.builder()
                .id(2L)
                .employer(mockEmployer)
                .subStatus("ACTIVE")
                .startedAt(LocalDateTime.now().minusDays(30))
                .expiresAt(LocalDateTime.now().plusDays(30))
                .plan(basicPlan)
                .build();

        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(basicSub));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        when(jobPostRepository.save(any(JobPost.class))).thenAnswer(invocation -> {
            JobPost saved = invocation.getArgument(0);
            saved.setId(201L);
            return saved;
        });

        JobPostResponse response = jobPostService.createJobPost(1L, createDto);

        // Feature: true (3 ngày)
        assertThat(response.getIsFeatured()).isTrue();
        assertThat(response.getFeatureExpiresAt()).isNotNull();
        // Highlight: false (Basic không hỗ trợ)
        assertThat(response.getIsHighlighted()).isFalse();
    }

    @Test
    void createJobPost_FreePlan_QuotaExceeded() {
        // Free plan: maxJobPostsPerMonth=0 → không thể tạo bài đăng
        Plan freePlan = Plan.builder()
                .maxJobPostsPerMonth(0)
                .allowHighlight(false)
                .featureDurationDays(0)
                .build();
        EmployerSubscription freeSub = EmployerSubscription.builder()
                .id(3L)
                .employer(mockEmployer)
                .subStatus("ACTIVE")
                .startedAt(LocalDateTime.now().minusDays(30))
                .expiresAt(LocalDateTime.now().plusYears(100))
                .plan(freePlan)
                .build();

        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE")).thenReturn(Optional.of(freeSub));
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(0);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobPostService.createJobPost(1L, createDto));
        assertTrue(ex.getMessage().contains("maximum limit"));
    }

    // ==================== getRecentJobsForSeeker ====================

    @Test
    void getRecentJobsForSeeker_Success_ReturnsPaginatedResponses() {
        when(jobPostRepository.findRecentJobsForSeeker(any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockJobPost)));
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl("logo.png")).thenReturn("https://cdn.test/logo.png");

        Page<JobPostResponse> result = jobPostService.getRecentJobsForSeeker(20, 0);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Developer");
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("Tech Corp");
        verify(jobPostRepository).findRecentJobsForSeeker(any(), any(), any(PageRequest.class));
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
        assertThat(response.getOverview().getSalary()).contains("1,500");
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

    // ==================== getAllJobPost (2-tier sorting) ====================

    @Test
    void getAllJobPost_WithFilter_ReturnsMappedPage() {
        // getAllJobPost now uses findAll(spec) (without Pageable) then sorts in-memory
        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(mockJobPost));
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        JobPostFilterRequest filter = new JobPostFilterRequest();
        Page<JobPostResponse> result = jobPostService.getAllJobPost(filter, 10, 0);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Developer");
        verify(jobPostRepository).findAll((Specification<JobPost>) any());  // verifies new method signature
    }

    @Test
    void getAllJobPost_2Tier_FeaturedFirst() {
        // Tạo 2 bài: một đang feature (tier1), một không feature (tier2)
        JobPost featuredPost = JobPost.builder()
                .id(1L)
                .employer(mockEmployer)
                .title("Featured Post")
                .description("Desc")
                .employmentType(EmploymentType.FULL_TIME)
                .status(JobPostStatus.OPEN)
                .educationLevel(EducationLevel.BACHELOR)
                .experience(2)
                .jobLevel(JobLevel.MIDDLE)
                .salaryMin(new BigDecimal("2000"))
                .salaryMax(new BigDecimal("3000"))
                .tags(List.of())
                .isFeatured(true)
                .isHighlighted(false)
                .featureActivatedAt(LocalDateTime.now().minusHours(2))  // newer feature
                .featureExpiresAt(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        JobPost nonFeaturedPost = JobPost.builder()
                .id(2L)
                .employer(mockEmployer)
                .title("Non-Featured Post")
                .description("Desc")
                .employmentType(EmploymentType.FULL_TIME)
                .status(JobPostStatus.OPEN)
                .educationLevel(EducationLevel.BACHELOR)
                .experience(2)
                .jobLevel(JobLevel.MIDDLE)
                .salaryMin(new BigDecimal("1500"))
                .salaryMax(new BigDecimal("2500"))
                .tags(List.of())
                .isFeatured(false)
                .isHighlighted(false)
                .featureActivatedAt(null)
                .featureExpiresAt(null)
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(nonFeaturedPost, featuredPost));
        when(jobIndustryRepository.findByJobPostId(anyLong())).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(anyLong())).thenReturn(0L);

        JobPostFilterRequest filter = new JobPostFilterRequest();
        Page<JobPostResponse> result = jobPostService.getAllJobPost(filter, 10, 0);

        // Bài featured phải đứng trước bài non-featured
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Featured Post");
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Non-Featured Post");
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

    // ==================== getEmployerJobPosts ====================

    @Test
    void getEmployerJobPosts_Success() {
        Page<JobPost> page = new PageImpl<>(List.of(mockJobPost));
        when(jobPostRepository.findByEmployer_Owner_Id(eq(1L), any())).thenReturn(page);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPostsByEmployer(1L, 10, 0);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Developer");
    }

    // ==================== Salary formatting in toResponse ====================

    @Test
    void toResponse_SalaryFormattedCorrectly() {
        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(mockJobPost));
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        JobPostResponse resp = result.getContent().get(0);

        assertThat(resp.getSalary()).contains("1,500");
        assertThat(resp.getSalary()).contains("2,500");
        assertThat(resp.getSalary()).contains("monthly");
    }

    @Test
    void toResponse_NullSalary_ThoaThuan() {
        mockJobPost.setSalaryMin(null);
        mockJobPost.setSalaryMax(null);

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(mockJobPost));
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        assertThat(result.getContent().get(0).getSalary()).isEqualTo("Negotiable");

        mockJobPost.setSalaryMin(new BigDecimal("1500"));
        mockJobPost.setSalaryMax(new BigDecimal("2500"));
    }

    // ==================== Experience & Days remaining ====================

    @Test
    void toResponse_NullExperience_ReturnsKhongYeuCau() {
        mockJobPost.setExperience(null);

        @SuppressWarnings("unchecked")
        Specification<JobPost> anySpec = any();
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(mockJobPost));
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
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(mockJobPost));
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
        when(jobPostRepository.findAll(anySpec)).thenReturn(List.of(mockJobPost));
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        Page<JobPostResponse> result = jobPostService.getAllJobPost(new JobPostFilterRequest(), 10, 0);
        assertThat(result.getContent().get(0).getDaysRemaining()).isEqualTo("Vô thời hạn");

        mockJobPost.setExpiresAt(LocalDateTime.now().plusDays(10));
    }

    // ==================== updateJobPostStatus ====================

    @Test
    void updateJobPostStatus_Success() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));
        when(jobPostRepository.save(any(JobPost.class))).thenReturn(mockJobPost);
        when(jobIndustryRepository.findByJobPostId(100L)).thenReturn(Collections.emptyList());
        when(jobApplicationRepository.countByJobPost_Id(100L)).thenReturn(0L);
        when(minioService.getFileUrl(any())).thenReturn("logo.png");

        JobPostResponse response = jobPostService.updateJobPostStatus(100L, 1L, JobPostStatus.CLOSED);

        assertThat(response).isNotNull();
        verify(jobPostRepository, times(1)).save(any(JobPost.class));
        verify(auditService, times(1)).createAuditLog(any());
    }

    @Test
    void updateJobPostStatus_NotOwner_ThrowsForbidden() {
        when(jobPostRepository.findById(100L)).thenReturn(Optional.of(mockJobPost));

        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> jobPostService.updateJobPostStatus(100L, 888L, JobPostStatus.CLOSED));
        assertTrue(ex.getMessage().contains("permission"));
    }
}