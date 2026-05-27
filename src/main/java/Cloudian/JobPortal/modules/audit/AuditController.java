package Cloudian.JobPortal.modules.audit;

import Cloudian.JobPortal.models.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("audit")
public class AuditController {
    @Autowired
    AuditService auditService;
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")   //admin guardssss
    public ResponseEntity<?> getAllAuditLogs(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset
    )
    {
        Page<AuditLog> response = auditService.getAllAuditLogs(limit , offset);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
