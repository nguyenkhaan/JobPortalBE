package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface EmployerRepository extends JpaRepository<EmployerProfile , Long>, JpaSpecificationExecutor<EmployerProfile> {
    public Optional<EmployerProfile> findByOwnerId(Long ownerId);
    public Optional<EmployerProfile> findByCompanyName(String companyName);
    long countByApprovalStatus(ApprovalStatus approvalStatus);
    List<EmployerProfile> findTop5ByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus approvalStatus);
}
