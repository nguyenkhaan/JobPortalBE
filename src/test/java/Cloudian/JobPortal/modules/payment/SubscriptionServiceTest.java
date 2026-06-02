package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.EmployerSubscription;
import Cloudian.JobPortal.models.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private EmployerProfile mockEmployer;

    @BeforeEach
    void setUp() {
        mockEmployer = EmployerProfile.builder().id(1L).build();
    }

    @Test
    void processPlanUpgrade_ShouldCalculateProrationCorrectly_WhenUpgradingToHigherPlan() {
        // --- 1. CHUẨN BỊ DỮ LIỆU GIẢ LẬP (ARRANGE) ---
        LocalDateTime now = LocalDateTime.now();

        // Gói cũ (Premium): Giá 300k, 1 tháng (30 ngày), Priority 1
        Plan oldPlan = Plan.builder()
                .id(1L)
                .name("Premium")
                .price(300000.0)
                .duration(1)
                .priority(1)
                .build();

        // Giả lập trạng thái: Gói cũ còn đúng 15 ngày nữa là hết hạn
        EmployerSubscription existingSub = EmployerSubscription.builder()
                .employer(mockEmployer)
                .plan(oldPlan)
                .startedAt(now.minusDays(15))
                .expiresAt(now.plusDays(15))
                .build();

        // Gói mới (VIP): Giá 600k, 1 tháng (30 ngày), Priority 2 (Cao hơn)
        Plan newPlan = Plan.builder()
                .id(2L)
                .name("VIP")
                .price(600000.0)
                .duration(1)
                .priority(2)
                .build();

        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(existingSub));

        // --- 2. THỰC THI HÀM CẦN TEST (ACT) ---
        subscriptionService.processPlanUpgrade(mockEmployer, newPlan);

        // --- 3. KIỂM CHỨNG KẾT QUẢ (ASSERT) ---
        // Bắt lại đối tượng được truyền vào hàm save() của repository
        ArgumentCaptor<EmployerSubscription> subCaptor = ArgumentCaptor.forClass(EmployerSubscription.class);
        verify(subscriptionRepository, times(1)).save(subCaptor.capture());

        EmployerSubscription savedSub = subCaptor.getValue();

        // Kiểm tra logic toán học:
        // Tiền dư = 15 ngày * (300k / 30) = 150k
        // Giá ngày gói mới = 600k / 30 = 20k
        // Số ngày quy đổi = 150k / 20k = 8 ngày (vì làm tròn)
        // Ngày hết hạn mới = now + 1 tháng (30 ngày) của gói mới + 8 ngày bù tiền = now + 38 ngày

        long expectedExtraDays = 8;
        long totalExpectedDaysFromNow = (newPlan.getDuration() * 30L) + expectedExtraDays; // 30 + 8 = 38

        long actualDaysAdded = ChronoUnit.DAYS.between(now, savedSub.getExpiresAt());

        assertEquals("VIP", savedSub.getPlan().getName(), "Phải được cập nhật sang gói mới");

        // Cho phép sai số 1 ngày do thời gian chạy code tính toán milli-seconds
        assertTrue(Math.abs(actualDaysAdded - totalExpectedDaysFromNow) <= 1,
                "Thời hạn gia hạn (bao gồm tiền bù) phải xấp xỉ 38 ngày. Thực tế: " + actualDaysAdded);
    }

    @Test
    void processPlanUpgrade_ShouldExtendDays_WhenBuyingLowerPlan() {
        // --- 1. CHUẨN BỊ DỮ LIỆU ---
        LocalDateTime now = LocalDateTime.now();

        Plan oldPlanVIP = Plan.builder()
                .name("VIP").priority(2)
                .duration(1).maxJobPostsPerMonth(10) // 10 bài/tháng
                .build();

        EmployerSubscription existingSub = EmployerSubscription.builder()
                .employer(mockEmployer).plan(oldPlanVIP)
                .expiresAt(now.plusDays(10)) // Còn 10 ngày gói VIP
                .build();

        Plan newPlanPremium = Plan.builder()
                .name("Premium").priority(1) // Mua gói thấp hơn
                .duration(1).maxJobPostsPerMonth(5) // 5 bài/tháng
                .build();

        when(subscriptionRepository.findByEmployerId(1L)).thenReturn(Optional.of(existingSub));

        // --- 2. THỰC THI ---
        subscriptionService.processPlanUpgrade(mockEmployer, newPlanPremium);

        // --- 3. KIỂM CHỨNG ---
        ArgumentCaptor<EmployerSubscription> subCaptor = ArgumentCaptor.forClass(EmployerSubscription.class);
        verify(subscriptionRepository).save(subCaptor.capture());
        EmployerSubscription savedSub = subCaptor.getValue();

        // Toán học: Mua 5 bài * 1 tháng = 5 bài mua thêm.
        // Quy ra ngày của gói VIP (10 bài/tháng): (5 / 10) * 30 ngày = 15 ngày.
        // Hết hạn = 10 ngày (cũ) + 15 ngày (mới quy đổi) = 25 ngày từ hôm nay.
        long actualDaysLeft = ChronoUnit.DAYS.between(now, savedSub.getExpiresAt());

        assertEquals("VIP", savedSub.getPlan().getName(), "Phải giữ nguyên tên gói cao nhất");
        assertTrue(Math.abs(actualDaysLeft - 25) <= 1,
                "Phải được cộng dồn 15 ngày quy đổi vào 10 ngày gốc thành 25. Thực tế: " + actualDaysLeft);
    }
}