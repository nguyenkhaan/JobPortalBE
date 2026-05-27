package Cloudian.JobPortal.modules.audit;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.AuditLog;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.audit.dto.AuditLogResponse;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    @Autowired
    AuditRepository auditRepository;
    @Autowired
    UserRepository userRepository;
    public Page<AuditLogResponse> getAllAuditLogs(Integer limit , Integer offset)
    {
        Pageable pageable = PageRequest.of(offset , limit);
        return auditRepository.findAll(pageable).map(this::toAuditLogResponse);
    }
    @Transactional
    public AuditLog createAuditLog(CreateAuditDto data)
    {
        User user = userRepository.findById(data.getUserId()).orElseThrow(() -> new NotFoundException("User not found"));
        AuditLog audit = AuditLog.builder()
                .user(user)
                .data(data.getData())
                .actionType(data.getActionType())
                .entityName(data.getEntityName())
                .recordId(data.getRecordId())
                .build();
        auditRepository.save(audit);
        return audit;
    }

    private AuditLogResponse toAuditLogResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .actionType(auditLog.getActionType())
                .entityName(auditLog.getEntityName())
                .recordId(auditLog.getRecordId())
                .eventTime(auditLog.getEventTime())
                .actorUserId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .actorEmail(auditLog.getUser() != null ? auditLog.getUser().getEmail() : null)
                .build();
    }
}
