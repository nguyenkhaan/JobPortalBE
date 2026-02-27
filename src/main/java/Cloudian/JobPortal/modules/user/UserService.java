package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.Users;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    public List<UserResponse> getAllUsers()
    {
        List<UserResponse> userLists = userRepository.findAll().stream().map(
                it -> new UserResponse(null , it.getEmail() , it.getCreatedAt(), it.getActive())
        ).toList();   //map tra ve Stream, khong phai List
        return userLists;
    }
}
