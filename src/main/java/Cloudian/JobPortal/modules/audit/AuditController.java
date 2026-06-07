package Cloudian.JobPortal.modules.audit;

import Cloudian.JobPortal.models.ActionType;
import Cloudian.JobPortal.models.EntityName;
import Cloudian.JobPortal.modules.audit.dto.AuditLogResponse;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("audit")
public class AuditController {
    @Autowired
    AuditService auditService;
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")   //admin guardssss
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ActionType actionType,
            @RequestParam(required = false) EntityName entityName,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset
    )
    {
        Page<AuditLogResponse> response = auditService.getAllAuditLogs(
                limit,
                offset,
                search,
                actionType,
                entityName,
                startDate,
                endDate
        );
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }

}
