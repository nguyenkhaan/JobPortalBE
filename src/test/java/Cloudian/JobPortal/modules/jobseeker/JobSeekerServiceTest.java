package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.*;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.dto.*;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSeekerServiceTest {

    @Mock private JobSeekerRepository jobSeekerRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private ProfileViewRepository profileViewRepository;
    @Mock private SavedJobRepository savedJobRepository;
    @Mock private MinioService minioService;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private JobAlertRepository jobAlertRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private JobSeekerService jobSeekerService;

    @Captor
    private ArgumentCaptor<JobSeekerProfile> profileCaptor;

    private User mockUser;
    private JobSeekerProfile mockProfile;
    private CreateJobSeekerRequest createRequest;
    private UpdateJobSeekerRequest updateRequest;
    private UpdateJobSeekerPhoneDto phoneDto;
    private ApplyJobRequest applyRequest;
    private JobPost mockJobPost;
    private EmployerProfile mockEmployer;
    private Resume mockResume;
    private SavedJob mockSavedJob;
    private JobApplication mockApplication;
    private JobAlert mockAlert;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jobSeekerService, "passwordEncoder", passwordEncoder);

        mockUser = User.builder()
                .id(1L)
                .email("seeker@test.com")
                .password("encodedPass")
                .build();

        mockProfile = JobSeekerProfile.builder()
                .id(10L)
                .fullName("Nguyen Van A")
                .address("123 Hanoi")
                .phone("0912345678")
                .professionalTitle("Java Developer")
                .biography("Experienced developer")
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .nationality("Vietnam")
                .maritalStatus("Single")
                .gender("Male")
                .experienceSummary("5 years")
                .educationSummary("Bachelor")
                .website("https://example.com")
                .secondaryPhone("0987654321")
                .approve(true)
                .user(mockUser)
                .build();

        createRequest = CreateJobSeekerRequest.builder()
                .fullName("New User")
                .address("Address")
                .phone("0912345678")
                .professionalTitle("Developer")
                .biography("Bio")
                .build();

        updateRequest = UpdateJobSeekerRequest.builder()
                .fullName("Updated Name")
                .address("Updated Address")
                .build();

        phoneDto = new UpdateJobSeekerPhoneDto();
        phoneDto.setPhone("0911111111");
        phoneDto.setSecondaryPhone("0922222222");
        phoneDto.setPassword("correctPass");

        mockEmployer = EmployerProfile.builder()
                .id(100L)
                .companyName("Tech Corp")
                .logo("logo.png")
                .address("HCMC")
                .build();

        mockJobPost = JobPost.builder()
                .id(200L)
                .title("Java Developer")
                .status(JobPostStatus.OPEN)
                .employmentType(EmploymentType.FULL_TIME)
                .salaryMin(new BigDecimal("15000000"))
                .salaryMax(new BigDecimal("20000000"))
                .salaryType(SalaryType.MONTHLY)
                .expiresAt(LocalDateTime.now().plusDays(10))
                .createdAt(LocalDateTime.now().minusDays(3))
                .employer(mockEmployer)
                .build();

        mockResume = Resume.builder()
                .id(300L)
                .fileUrl("resume.pdf")
                .jobSeeker(mockProfile)
                .build();

        mockProfile.setResumes(List.of(mockResume));

        applyRequest = ApplyJobRequest.builder()
                .jobId(200L)
                .resumeId(300L)
                .coverLetter("I want this job")
                .build();

        mockSavedJob = SavedJob.builder()
                .id(1L)
                .jobSeeker(mockProfile)
                .jobPost(mockJobPost)
                .savedAt(LocalDateTime.now())
                .build();

        mockApplication = JobApplication.builder()
                .id(1L)
                .jobPost(mockJobPost)
                .jobSeeker(mockProfile)
                .resume(mockResume)
                .coverLetter("Cover letter")
                .status(JobApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now().minusDays(1))
                .build();

        mockAlert = JobAlert.builder()
                .id(1L)
                .jobSeeker(mockProfile)
                .keyword("Java")
                .location("Hanoi")
                .category("IT")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== createProfile ====================

    @Test
    void createProfile_Success_ReturnsJobSeekerResponse() {
        when(jobSeekerRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(jobSeekerRepository.save(any(JobSeekerProfile.class))).thenReturn(mockProfile);
        when(auditService.createAuditLog(any())).thenReturn(null);

        JobSeekerResponse response = jobSeekerService.createProfile(createRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        verify(jobSeekerRepository, times(1)).save(any(JobSeekerProfile.class));
        verify(auditService, times(1)).createAuditLog(any());
    }

    @Test
    void createProfile_DuplicatePhone_ThrowsBadRequest() {
        when(jobSeekerRepository.existsByPhone("0912345678")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.createProfile(createRequest, 1L));
        assertTrue(ex.getMessage().contains("Phone number already exists"));
        verify(jobSeekerRepository, never()).save(any());
    }

    @Test
    void createProfile_UserNotFound_ThrowsUnauthorized() {
        when(jobSeekerRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> jobSeekerService.createProfile(createRequest, 1L));
        assertTrue(ex.getMessage().contains("user does not exist"));
        verify(jobSeekerRepository, never()).save(any());
    }

    @Test
    void createProfile_ProfileAlreadyExists_ThrowsConflict() {
        when(jobSeekerRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> jobSeekerService.createProfile(createRequest, 1L));
        assertTrue(ex.getMessage().contains("already have job seeker profile"));
        verify(jobSeekerRepository, never()).save(any());
    }

    // ==================== getProfile ====================

    @Test
    void getProfile_Success_ReturnsProfile() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));

        JobSeekerResponse response = jobSeekerService.getProfile(1L);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(response.getEmail()).isEqualTo("seeker@test.com");
    }

    @Test
    void getProfile_NotFound_ThrowsResourceNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.getProfile(1L));
        assertTrue(ex.getMessage().contains("profile's user does not exist"));
    }

    // ==================== discoverProfiles ====================

    @Test
    void discoverProfiles_WithSearch_ReturnsPage() {
        Page<JobSeekerProfile> profilePage = new PageImpl<>(List.of(mockProfile));
        when(jobSeekerRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(profilePage);

        Page<JobSeekerResponse> result = jobSeekerService.discoverProfiles("Java", 20, 0);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getFullName()).isEqualTo("Nguyen Van A");
    }

    @Test
    void discoverProfiles_WithoutSearch_ReturnsPage() {
        Page<JobSeekerProfile> profilePage = new PageImpl<>(List.of(mockProfile));
        when(jobSeekerRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(profilePage);

        Page<JobSeekerResponse> result = jobSeekerService.discoverProfiles(null, 20, 0);

        assertThat(result).isNotEmpty();
    }

    @Test
    void discoverProfiles_InvalidLimit_ThrowsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.discoverProfiles("test", 0, 0));
        assertTrue(ex.getMessage().contains("Invalid limit"));
    }

    @Test
    void discoverProfiles_InvalidOffset_ThrowsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.discoverProfiles("test", 10, -1));
        assertTrue(ex.getMessage().contains("Invalid offset"));
    }

    // ==================== updateProfile ====================

    @Test
    void updateProfile_Success_ReturnsUpdatedProfile() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        // Simulate save returning the same object (which was modified in-memory by service)
        when(jobSeekerRepository.save(any(JobSeekerProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(auditService.createAuditLog(any())).thenReturn(null);

        JobSeekerResponse response = jobSeekerService.updateProfile(updateRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Updated Name");
        assertThat(response.getAddress()).isEqualTo("Updated Address");
        verify(jobSeekerRepository, times(1)).save(any(JobSeekerProfile.class));
    }

    @Test
    void updateProfile_EmptyFullName_ThrowsBadRequest() {
        UpdateJobSeekerRequest badRequest = UpdateJobSeekerRequest.builder().fullName("").build();
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.updateProfile(badRequest, 1L));
        assertTrue(ex.getMessage().contains("full name cannot be empty"));
    }

    @Test
    void updateProfile_NotFound_ThrowsResourceNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.updateProfile(updateRequest, 1L));
    }

    // ==================== updatePhone ====================

    @Test
    void updatePhone_Success_ReturnsPhoneMap() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        mockUser.setJobSeekerProfile(mockProfile);
        when(passwordEncoder.matches("correctPass", "encodedPass")).thenReturn(true);
        when(jobSeekerRepository.existsByPhoneOrSecondaryPhoneAndIdNot(anyString(), anyString(), anyLong())).thenReturn(false);

        Map<String, String> result = jobSeekerService.updatePhone(1L, phoneDto);

        assertThat(result).containsKeys("phone", "secondaryPhone");
        assertThat(result.get("phone")).isEqualTo("0911111111");
    }

    @Test
    void updatePhone_UserNotFound_ThrowsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> jobSeekerService.updatePhone(1L, phoneDto));
    }

    @Test
    void updatePhone_WrongPassword_ThrowsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("correctPass", "encodedPass")).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.updatePhone(1L, phoneDto));
        assertTrue(ex.getMessage().contains("Wrong password"));
    }

    // ==================== deleteProfile ====================

    @Test
    void deleteProfile_Success_SoftDeletes() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));

        jobSeekerService.deleteProfile(1L);

        assertThat(mockProfile.getDeleteAt()).isNotNull();
        verify(jobSeekerRepository, times(1)).flush();
    }

    @Test
    void deleteProfile_NotFound_ThrowsResourceNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.deleteProfile(1L));
    }

    // ==================== toggleSavedJob ====================

    @Test
    void toggleSavedJob_SaveNew_ReturnsIsSavedTrue() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(savedJobRepository.findByJobSeekerIdAndJobPostId(10L, 200L)).thenReturn(Optional.empty());
        when(savedJobRepository.save(any(SavedJob.class))).thenReturn(mockSavedJob);

        Map<String, Object> result = jobSeekerService.toggleSavedJob(1L, 200L);

        assertThat(result.get("isSaved")).isEqualTo(true);
        verify(savedJobRepository, times(1)).save(any(SavedJob.class));
    }

    @Test
    void toggleSavedJob_UnsaveExisting_ReturnsIsSavedFalse() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(savedJobRepository.findByJobSeekerIdAndJobPostId(10L, 200L)).thenReturn(Optional.of(mockSavedJob));

        Map<String, Object> result = jobSeekerService.toggleSavedJob(1L, 200L);

        assertThat(result.get("isSaved")).isEqualTo(false);
        verify(savedJobRepository, times(1)).delete(any(SavedJob.class));
    }

    @Test
    void toggleSavedJob_ProfileNotFound_ThrowsResourceNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.toggleSavedJob(1L, 200L));
    }

    // ==================== getSavedJobs ====================

    @Test
    void getSavedJobs_Success_ReturnsPage() {
        when(savedJobRepository.findByUserIdWithJobPost(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockSavedJob)));

        Page<Map<String, Object>> result = jobSeekerService.getSavedJobs(1L, 20, 0);

        assertThat(result).isNotEmpty();
        Map<String, Object> item = result.getContent().get(0);
        assertThat(item.get("id")).isEqualTo("200");
        assertThat(item.get("role")).isEqualTo("Java Developer");
        assertThat(item.get("type")).isEqualTo("Full Time");
    }

    // ==================== applyJob ====================

    @Test
    void applyJob_Success_ReturnsResult() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobPostRepository.findById(200L)).thenReturn(Optional.of(mockJobPost));
        when(jobApplicationRepository.findByJobSeeker_User_Id(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(mockApplication);

        Map<String, Object> result = jobSeekerService.applyJob(1L, applyRequest);

        assertThat(result.get("status")).isEqualTo("PENDING");
        assertThat(result.get("message")).isEqualTo("Application submitted successfully");
        verify(jobApplicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    void applyJob_ProfileNotFound_ThrowsResourceNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.applyJob(1L, applyRequest));
    }

    @Test
    void applyJob_JobNotFound_ThrowsNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobPostRepository.findById(200L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> jobSeekerService.applyJob(1L, applyRequest));
    }

    @Test
    void applyJob_JobClosed_ThrowsBadRequest() {
        mockJobPost.setStatus(JobPostStatus.CLOSED);
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobPostRepository.findById(200L)).thenReturn(Optional.of(mockJobPost));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.applyJob(1L, applyRequest));
        assertTrue(ex.getMessage().contains("no longer accepting applications"));
    }

    @Test
    void applyJob_JobExpired_ThrowsBadRequest() {
        mockJobPost.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobPostRepository.findById(200L)).thenReturn(Optional.of(mockJobPost));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.applyJob(1L, applyRequest));
        assertTrue(ex.getMessage().contains("expired"));
    }

    @Test
    void applyJob_ResumeNotOwned_ThrowsBadRequest() {
        applyRequest.setResumeId(999L);
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobPostRepository.findById(200L)).thenReturn(Optional.of(mockJobPost));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.applyJob(1L, applyRequest));
        assertTrue(ex.getMessage().contains("Resume not found"));
    }

    @Test
    void applyJob_DuplicateApplication_ThrowsBadRequest() {
        // Create an application for the same job post
        JobApplication dupApp = JobApplication.builder()
                .id(2L)
                .jobPost(mockJobPost)
                .jobSeeker(mockProfile)
                .build();

        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobPostRepository.findById(200L)).thenReturn(Optional.of(mockJobPost));
        when(jobApplicationRepository.findByJobSeeker_User_Id(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(dupApp)));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> jobSeekerService.applyJob(1L, applyRequest));
        assertTrue(ex.getMessage().contains("already applied"));
    }

    // ==================== getApplications ====================

    @Test
    void getApplications_Success_ReturnsPage() {
        when(jobApplicationRepository.findByUserIdWithJobPost(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockApplication)));

        Page<Map<String, Object>> result = jobSeekerService.getApplications(1L, 20, 0);

        assertThat(result).isNotEmpty();
        Map<String, Object> item = result.getContent().get(0);
        assertThat(item.get("id")).isEqualTo("1");
        assertThat(item.get("role")).isEqualTo("Java Developer");
        assertThat(item.get("status")).isEqualTo("PENDING");
    }

    // ==================== getStatistics ====================

    @Test
    void getStatistics_Success_ReturnsFullDashboard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobApplicationRepository.countByJobSeekerId(10L)).thenReturn(5L);
        when(savedJobRepository.countByJobSeekerId(10L)).thenReturn(3L);
        when(jobApplicationRepository.findTop5ByUserIdWithJobPost(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(mockApplication));
        when(jobAlertRepository.countByUserId(1L)).thenReturn(2L);

        JobSeekerStatisticResponse result = jobSeekerService.getStatistics(1L);

        assertThat(result.getAppliedCount()).isEqualTo(5);
        assertThat(result.getFavoriteCount()).isEqualTo(3);
        assertThat(result.getAlertCount()).isEqualTo(2);
        assertThat(result.isProfileCompleted()).isTrue();
        assertThat(result.getRecentApplied()).hasSize(1);
        assertThat(result.getRecentApplied().get(0).getRole()).isEqualTo("Java Developer");
    }

    @Test
    void getStatistics_UserNotFound_ThrowsUnauthorized() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> jobSeekerService.getStatistics(1L));
    }

    @Test
    void getStatistics_ProfileNotFound_ThrowsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.getStatistics(1L));
    }

    @Test
    void getStatistics_ProfileIncomplete_ReturnsFalse() {
        mockProfile.setProfessionalTitle(null);
        mockProfile.setBiography(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobApplicationRepository.countByJobSeekerId(10L)).thenReturn(0L);
        when(savedJobRepository.countByJobSeekerId(10L)).thenReturn(0L);
        when(jobApplicationRepository.findTop5ByUserIdWithJobPost(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of());
        when(jobAlertRepository.countByUserId(1L)).thenReturn(0L);

        JobSeekerStatisticResponse result = jobSeekerService.getStatistics(1L);
        assertThat(result.isProfileCompleted()).isFalse();
    }

    // ==================== createJobAlert ====================

    @Test
    void createJobAlert_Success_ReturnsAlert() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.of(mockProfile));
        when(jobAlertRepository.save(any(JobAlert.class))).thenReturn(mockAlert);

        Map<String, Object> result = jobSeekerService.createJobAlert(1L, "Java", "Hanoi", "IT");

        assertThat(result.get("keyword")).isEqualTo("Java");
        assertThat(result.get("location")).isEqualTo("Hanoi");
        assertThat(result.get("category")).isEqualTo("IT");
        verify(jobAlertRepository, times(1)).save(any(JobAlert.class));
    }

    @Test
    void createJobAlert_ProfileNotFound_ThrowsResourceNotFound() {
        when(jobSeekerRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> jobSeekerService.createJobAlert(1L, "Java", null, null));
    }

    // ==================== getJobAlerts ====================

    @Test
    void getJobAlerts_Success_ReturnsPage() {
        when(jobAlertRepository.findByUserId(eq(1L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(mockAlert)));

        Page<Map<String, Object>> result = jobSeekerService.getJobAlerts(1L, 20, 0);

        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).get("keyword")).isEqualTo("Java");
    }

    // ==================== deleteJobAlert ====================

    @Test
    void deleteJobAlert_Success_DeletesAlert() {
        when(jobAlertRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mockAlert));

        jobSeekerService.deleteJobAlert(1L, 1L);

        verify(jobAlertRepository, times(1)).delete(any(JobAlert.class));
    }

    @Test
    void deleteJobAlert_NotFound_ThrowsNotFound() {
        when(jobAlertRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> jobSeekerService.deleteJobAlert(1L, 1L));
        assertTrue(ex.getMessage().contains("does not belong to you"));
    }

    // ==================== isValidPhone (static) ====================

    @Test
    void isValidPhone_Valid_ReturnsTrue() {
        assertTrue(JobSeekerService.isValidPhone("0912345678"));
    }

    @Test
    void isValidPhone_Invalid_ReturnsFalse() {
        assertFalse(JobSeekerService.isValidPhone("12345"));
        assertFalse(JobSeekerService.isValidPhone(""));
        assertFalse(JobSeekerService.isValidPhone(null));
    }
}