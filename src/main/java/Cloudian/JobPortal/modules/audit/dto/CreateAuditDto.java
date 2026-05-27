package Cloudian.JobPortal.modules.audit.dto;

import Cloudian.JobPortal.models.ActionType;
import Cloudian.JobPortal.models.EntityName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAuditDto {
    @NotNull(
            message = "Action type is required"
    )
    private ActionType actionType;
    @NotNull(
            message = "userId is required"
    )
    private Long userId;
    @NotNull(
            message = "Enity name is required"
    )
    private EntityName entityName;
    @NotNull(
            message = "recordId is required"
    )
    private Long recordId;
    @NotNull(
            message = "data is required"
    )
    private Map<String , Object> data;
}
