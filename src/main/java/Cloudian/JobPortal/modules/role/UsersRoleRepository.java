package Cloudian.JobPortal.modules.role;

import Cloudian.JobPortal.models.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRoleRepository extends JpaRepository<UserRole, Long>
{
//    public ArrayList<Role> getByUsersID(Long userID);
}
