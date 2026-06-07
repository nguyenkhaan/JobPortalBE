package Cloudian.JobPortal.modules.audit;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.ActionType;
import Cloudian.JobPortal.models.AuditLog;
import Cloudian.JobPortal.models.EntityName;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    @Autowired
    AuditRepository auditRepository;
    @Autowired
    UserRepository userRepository;
    public Page<AuditLogResponse> getAllAuditLogs(
            Integer limit,
            Integer offset,
            String search,
            ActionType actionType,
            EntityName entityName,
            LocalDate startDate,
            LocalDate endDate
    )
    {
        if (limit == null || limit < 1 || limit > 100) {
            throw new BadRequestException("Invalid limit");
        }
        if (offset == null || offset < 0) {
            throw new BadRequestException("Invalid offset");
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String value = "%" + search.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("user").get("email")), value),
                                cb.equal(root.get("user").get("id"), value)
                        )
                );
            }

            if (actionType != null) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }

            if (entityName != null) {
                predicates.add(cb.equal(root.get("entityName"), entityName));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventTime"), startDate.atStartOfDay()));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventTime"), endDate.atTime(23, 59, 59)));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return auditRepository.findAll(spec, pageable).map(this::toAuditLogResponse);
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
