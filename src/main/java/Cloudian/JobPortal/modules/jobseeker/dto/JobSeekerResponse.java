package Cloudian.JobPortal.modules.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerResponse {
    private Long id;
    private String fullName;
    private String address;
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
    private String secondaryPhone;
    private Boolean approve;
}
