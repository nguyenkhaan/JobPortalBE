package Cloudian.JobPortal.modules.jwt.dto;

import Cloudian.JobPortal.models.Role;
import Cloudian.JobPortal.models.TokenType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;

@Data
@AllArgsConstructor
public class TokenBody
{
    @NotBlank
    @NotNull
    private String email;
    private Long id;
    private ArrayList<Role> roles = new ArrayList<>();
    @NotBlank
    @NotNull
    private TokenType purpose;
}
