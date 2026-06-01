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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentWebhookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean(answers = Answers.RETURNS_DEEP_STUBS)
    private PayOS payOS;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        userRepository.deleteAll();

        User dummyUser = new User();
        // Cấu hình user mồi nếu bảng của bạn có các trường bắt buộc (Not Null)
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
        // --- BƯỚC 1: GIẢ LẬP HÀNH VI CỦA SDK PAYOS ---
        Mockito.when(payOS.webhooks().verify(any()).getOrderCode()).thenReturn(testPayment.getId());

        // --- BƯỚC 2: TẠO GÓI TIN JSON MỒI ---
        Map<String, Object> mockPayload = Map.of(
                "code", "00",
                "desc", "success",
                "data", Map.of(
                        "orderCode", testPayment.getId(),
                        "amount", 50000
                ),
                "signature", "mocked_checksum_signature"
        );

        // --- BƯỚC 3: BẮN REQUEST VÀO CONTROLLER ---
        mockMvc.perform(post("/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").value(0))
                .andExpect(jsonPath("$.message").value("Webhook processed successfully"));

        // --- BƯỚC 4: ĐỐI SOÁT KẾT QUẢ DATABASE ---
        Payment updatedPayment = paymentRepository.findById(testPayment.getId()).orElseThrow();
        assertEquals(PaymentStatus.COMPLETED, updatedPayment.getStatus());
    }
}