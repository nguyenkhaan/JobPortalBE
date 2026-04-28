package Cloudian.JobPortal.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetEmailRequest {
    @NotBlank
    private String password;
    @Email
    @NotBlank
    private String newEmail;
}
