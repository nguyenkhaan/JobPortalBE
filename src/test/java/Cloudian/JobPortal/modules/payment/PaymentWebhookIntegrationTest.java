package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.Payment;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.models.User;
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

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private PayOS payOS;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        User dummyUser = new User();
        dummyUser.setEmail("test_webhook_" + System.currentTimeMillis() + "@jobportal.com");
        dummyUser.setPassword("RawPassword123!");
        userRepository.save(dummyUser);

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(0))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));

        Payment updatedPayment = paymentRepository.findById(testPayment.getId()).orElseThrow();
        assertEquals(PaymentStatus.COMPLETED, updatedPayment.getStatus());
    }
}