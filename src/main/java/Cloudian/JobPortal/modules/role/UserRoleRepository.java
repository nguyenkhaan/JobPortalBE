package Cloudian.JobPortal.modules.role;

import Cloudian.JobPortal.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long>
{
    List<UserRole> findByUserId(Long userId);
//    public ArrayList<Role> getByUserID(Long userID);
}
