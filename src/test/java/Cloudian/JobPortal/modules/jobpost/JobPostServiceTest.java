package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.EmployerSubscription;
import Cloudian.JobPortal.models.Plan;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.jobpost.dto.CreateJobPostDto;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostServiceTest {

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private JobPostRepository jobPostRepository;

    // Inject các Mock vào Service cần test
    @InjectMocks
    private JobPostService jobPostService;

    private EmployerProfile mockEmployer;
    private CreateJobPostDto dummyDto;

    @BeforeEach
    void setUp() {
        // Giả lập 1 tài khoản Employer có ID là 1
        mockEmployer = EmployerProfile.builder().id(1L).build();
        dummyDto = new CreateJobPostDto(); // Dữ liệu giả định để tạo bài
    }

    @Test
    void createJobPost_ShouldThrowException_WhenSubscriptionExpired() {
        // 1. ARRANGE: Chuẩn bị dữ liệu
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));

        // Giả lập gói dịch vụ ĐÃ HẾT HẠN (từ 1 ngày trước)
        EmployerSubscription expiredSub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(expiredSub));

        // 2. ACT & 3. ASSERT: Thực thi và Kiểm chứng lỗi
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            jobPostService.createJobPost(1L, dummyDto);
        });

        // Đảm bảo thông báo lỗi chứa từ khóa "expired"
        assertTrue(exception.getMessage().contains("expired"), "Phải báo lỗi hết hạn gói");
    }

    @Test
    void createJobPost_ShouldThrowException_WhenQuotaExceeded() {
        // 1. ARRANGE: Chuẩn bị dữ liệu
        when(employerRepository.findByOwnerId(1L)).thenReturn(Optional.of(mockEmployer));

        // Giả lập gói Free (Cho phép đăng tối đa 2 bài/tháng) và VẪN CÒN HẠN
        Plan freePlan = Plan.builder().maxJobPostsPerMonth(2).build();
        EmployerSubscription validSub = EmployerSubscription.builder()
                .expiresAt(LocalDateTime.now().plusDays(10))
                .plan(freePlan)
                .build();
        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(validSub));

        // Giả lập Employer này ĐÃ ĐĂNG 2 BÀI trong tháng này (Đạt giới hạn)
        when(jobPostRepository.countByEmployerIdAndCreatedAtBetween(eq(1L), any(), any())).thenReturn(2);

        // 2. ACT & 3. ASSERT: Thực thi và Kiểm chứng lỗi
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            jobPostService.createJobPost(1L, dummyDto);
        });

        // Đảm bảo thông báo lỗi chứa từ khóa "maximum limit"
        assertTrue(exception.getMessage().contains("maximum limit"), "Phải báo lỗi vượt quá số lượng bài");
    }
}