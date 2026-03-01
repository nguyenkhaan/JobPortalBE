package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.modules.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    public UserService userService;
    @GetMapping()
    public ResponseEntity<?> getAllUser()
    {
        List<UserResponse> responseData = userService.getAllUsers();
        return ResponseEntity.ok().body(responseData);
    }
}
