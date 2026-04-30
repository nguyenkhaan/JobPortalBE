package Cloudian.JobPortal.modules.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobSeekerRequest {
    private String fullName;
    private String address;
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid phone number")
    private String phone;
}
