package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.models.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployerRepository extends JpaRepository<EmployerProfile , Long> {
    public Optional<EmployerProfile> findByOwnerId(Long ownerId);
    public Optional<EmployerProfile> findByCompanyName(String companyName);
}
