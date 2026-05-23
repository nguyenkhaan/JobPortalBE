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
    @NotBlank(
            message = "password cannot be blank"
    )
    private String password;
    @Email(
            message = "email must be in valid format: cloudian@example.com"
    )
    @NotBlank(
            message = "email cannot be blank or empty"
    )
    private String newEmail;
}
