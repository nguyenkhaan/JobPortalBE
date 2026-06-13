package Cloudian.JobPortal.modules.auth.dto;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.models.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthMeResponse {
    private Long id;
    private String email;
    private Boolean isEmailVerified;
    private List<Role> roles;
    private LocalDateTime createdAt;

    private Boolean hasProfile;
    private ApprovalStatus employerApprovalStatus;
    private String avatar;
    private String name;
}