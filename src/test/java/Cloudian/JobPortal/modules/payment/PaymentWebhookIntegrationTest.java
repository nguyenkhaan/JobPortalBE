package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.Payment;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.models.Plan;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.payos.PayOS;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class PaymentWebhookIntegrationTest {

    static {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    // 👇 Khai báo thêm 2 Repository để nạp dữ liệu giả
    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private PlanRepository planRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private PayOS payOS;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // 1. Tạo User giả
        User dummyUser = new User();
        dummyUser.setEmail("test_webhook_" + System.currentTimeMillis() + "@jobportal.com");
        dummyUser.setPassword("RawPassword123!");
        userRepository.save(dummyUser);

        // 2. TẠO THÊM: EmployerProfile liên kết với User
        EmployerProfile employer = EmployerProfile.builder()
                .owner(dummyUser)
                .companyName("Công ty Test Webhook")
                .companyWebsite("https://test.com")
                .address("123 Test Street")
                .phone("0987654321")
                .email(dummyUser.getEmail())
                .build();
        employerRepository.save(employer);

        // 3. TẠO THÊM: Gói Plan "PREMIUM" mà test đang gọi tới
        Plan premiumPlan = Plan.builder()
                .name("PREMIUM")
                .price(50000.0)
                .duration(1)
                .maxJobPostsPerMonth(30)
                .priority(2)
                .build();
        planRepository.save(premiumPlan);

        // 4. Tạo Hóa đơn thanh toán giả
        testPayment = Payment.builder()
                .planName("PREMIUM")
                .cost(50000.0)
                .status(PaymentStatus.PENDING)
                .user(dummyUser)
                .build();
        paymentRepository.save(testPayment);
    }

    @Test
    void shouldCompletePaymentSuccessfullyWhenWebhookIsCalled() throws Exception {
        Mockito.when(payOS.webhooks().verify(any()).getOrderCode()).thenReturn(testPayment.getId());

        Map<String, Object> mockPayload = Map.of(
                "code", "00",
                "desc", "success",
                "data", Map.of(
                        "orderCode", testPayment.getId(),
                        "amount", 50000
                ),
                "signature", "mocked_checksum_signature"
        );

        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayload)))
                .andExpect(status().isOk()) // Kỳ vọng 200 OK
                .andExpect(jsonPath("$.error").value(0))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));

        Payment updatedPayment = paymentRepository.findById(testPayment.getId()).orElseThrow();
        assertEquals(PaymentStatus.COMPLETED, updatedPayment.getStatus());
    }
}