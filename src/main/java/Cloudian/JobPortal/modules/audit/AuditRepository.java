package Cloudian.JobPortal.modules.audit;

import Cloudian.JobPortal.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditLog , Long> {
}
