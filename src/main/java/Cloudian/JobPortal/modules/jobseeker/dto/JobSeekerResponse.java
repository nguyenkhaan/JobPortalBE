package Cloudian.JobPortal.modules.jobseeker.dto;

import Cloudian.JobPortal.modules.user.dto.JobSeekerProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerResponse {
    private Long id;
    private String fullName;
    private String address;
    private String phone;

    // private String email ?
}
