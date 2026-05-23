package Cloudian.JobPortal.modules.industry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateIndustry {
    @NotBlank(
            message = "name to update cannot be blank"
    )
    @NotNull(
            message = "name to update cannot be null"
    )
    String name;
}
