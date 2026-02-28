package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.Users;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService
{
    @Autowired
    private UserRepository userRepository;
    public List<UserResponse> getAllUsers()
    {
        List<UserResponse> userLists = userRepository.findAll().stream().map(
                it -> new UserResponse(null , it.getEmail() , it.getCreatedAt(), it.getActive())
        ).toList();   //map tra ve Stream, khong phai List
        return userLists;
    }
    public Users findUserByEmail(String email)
    {
        //Make something to this
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not "));
        /*
        Install this to use UsernameNotFoundException if you haven't yet
        	implementation 'org.springframework.boot:spring-boot-starter-security'
	        implementation 'org.springframework.security:spring-security-core:6.2.8'
         */

    }

}
