package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.Users;
import Cloudian.JobPortal.models.UsersRole;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    //Get users or nothing => We can only use orElseThrow if we have Optional Data Type
    Optional<Users> findByEmail(String email);  //Su dung Optional long vao de co the dung duoi orElseThrow

    boolean existsByEmail(String email);
}
