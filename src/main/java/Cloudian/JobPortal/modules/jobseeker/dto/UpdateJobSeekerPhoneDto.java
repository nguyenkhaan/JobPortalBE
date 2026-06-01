package Cloudian.JobPortal.modules.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateJobSeekerPhoneDto {
    private String phone;
    private String secondaryPhone;
    @NotBlank()
    private String password;
}
