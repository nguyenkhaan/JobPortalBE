package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
    boolean existsByEmail(String email);
}
