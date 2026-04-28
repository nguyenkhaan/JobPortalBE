package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    //Get users or nothing => We can only use orElseThrow if we have Optional Data Type
    Optional<User>  findByEmail(String email);  //Sudung Optional long vao de co the dung duoi orElseThrow

    boolean existsByEmail(String email);
}
