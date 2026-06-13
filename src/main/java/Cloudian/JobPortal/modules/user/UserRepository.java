package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.Role;
import Cloudian.JobPortal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    //Get users or nothing => We can only use orElseThrow if we have Optional Data Type
    Optional<User>  findByEmail(String email);  //Sudung Optional long vao de co the dung duoi orElseThrow

    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN u.userRoleList ur
            WHERE ur.role = :role
            """)
    List<User> findDistinctByRole(@Param("role") Role role);
}
