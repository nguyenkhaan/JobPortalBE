package Cloudian.JobPortal.modules.audit.dto;

import Cloudian.JobPortal.models.ActionType;
import Cloudian.JobPortal.models.EntityName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private ActionType actionType;
    private EntityName entityName;
    private Long recordId;
    private LocalDateTime eventTime;

    private Long actorUserId;
    private String actorEmail;
}

