package Cloudian.JobPortal.modules.jobseeker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobSeekerRequest {
    private String fullName;
    private String address;
    private String professionalTitle;
    private String biography;
    private LocalDate dateOfBirth;
    private String nationality;
    private String maritalStatus;
    private String gender;
    private String experienceSummary;
    private String educationSummary;
    private String website;
    private String facebookUrl;
    private String twitterUrl;
    private String linkedlnUrl;
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Invalid phone number")
    private String secondaryPhone;

    @Schema(description = "Avatar image file")
    private MultipartFile avatar;
}
