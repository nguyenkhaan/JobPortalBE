package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = @Index(columnList = "companyName" , name = "idx_company_name")
)
@SQLRestriction("delete_at is NULL")
@SQLDelete(
        sql = """
                UPDATE employer_profile SET delete_at = NOW() WHERE id = ? 
                """
)
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne //Luon map den id cua bang kia
//    @JoinColumn(nullable = false , name = "owner_id") //ManyToOne , JoinColumn(name, nullable)
    @JoinColumn(name = "owner_id" , unique = true)
    private User owner;
    @Column(name = "logo")
    private String logo;
    @Column(nullable = false , name = "company_name")
    private String companyName;
    @Column(nullable = false , name = "company_website")
    private String companyWebsite;
    @Column(nullable = false  , name = "address")
    private String address;
    //Optional
    @Email
    private String email;
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String description = "";
    private String phone;
    @Builder.Default
    private Integer capacity = 0;
    //Soft delete
    @Column(name = "delete_at")
    @Builder.Default
    LocalDateTime deleteAt = null;

    // trạng thái hoạt động , dùng để admin khóa mở tài khoản:
    @Builder.Default
    private Boolean active = false;

    // Trạng thái duyệt: Dùng cho luồng đăng ký ban đầu của nhà tuyển dụng
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    // Thời gian nộp hồ sơ
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Thời gian cập nhật hồ sơ gần nhất
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "banner")
    private String banner;

    @Column(name = "business_license")
    private String businessLicense;

    @Column(name = "current_plan", nullable = false)
    @Builder.Default
    private String currentPlan = "Free";

    @Column(name = "plan_amount")
    @Builder.Default
    private Double planAmount = 0.0;

    @Column(name = "package_started_at")
    private LocalDateTime packageStartedAt;

    @Column(name = "package_expires_at")
    private LocalDateTime packageExpiresAt;

    @Column(name = "is_subscription_canceled", nullable = false)
    @Builder.Default
    private Boolean isSubscriptionCanceled = false;

    @Column(name = "industry")
    private String industry;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    //Foreign key
    @OneToMany(mappedBy = "employer")  //mappedBy phai trung voi ten ben bang ben kia
    @Builder.Default
    private List<JobPost> jobPostList = new ArrayList<>();

}
