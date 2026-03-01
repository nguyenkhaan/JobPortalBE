package Cloudian.JobPortal.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    //Tra ve thong tin nguoi dung da dang ki
    private Long id;
    private String email;
    private LocalDateTime createdAt;
    private Boolean active;
}
//JobSeekerProfile Response -> Cau hinh them , tam thoi UserResponse chi can cau hinh nhu vay