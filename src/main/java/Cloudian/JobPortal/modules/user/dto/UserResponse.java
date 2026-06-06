package Cloudian.JobPortal.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import Cloudian.JobPortal.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    //Tra ve thong tin nguoi dung da dang ki
    private Long id;
    private String email;
    private String displayName;
    private LocalDateTime createdAt;
    private Boolean active;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean banned;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Role> roles;
}
//JobSeekerProfile Response -> Cau hinh them , tam thoi UserResponse chi can cau hinh nhu vay
