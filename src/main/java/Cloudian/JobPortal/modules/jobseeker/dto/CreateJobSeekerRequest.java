package Cloudian.JobPortal.modules.jobseeker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobSeekerRequest {

    @NotBlank(message = "full name is required")
    private String fullName;

    @NotBlank(message = "address cannot left blank")
    private String address;

    @NotBlank(message = "phone number is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid phone number")
    private String phone;
    
    private String professionalTitle;
    private String biography;
    private LocalDate dateOfBirth;
    private String nationality;
    private String maritalStatus;
    private String gender;
    private String experienceSummary;
    private String educationSummary;
    private String website;
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid phone number")
    private String secondaryPhone;
}
