package Cloudian.JobPortal.modules.auth.dto;

import Cloudian.JobPortal.modules.user.dto.UserResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRegisterResponse {
    private UserResponse user;
    private String token; //Sau nay token nay se duoc gui ve mail
}
