package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.Users;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterRequest;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterResponse;
import Cloudian.JobPortal.modules.user.UserRepository;
import Cloudian.JobPortal.modules.user.dto.UserResponse;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    //User Repository
    @Autowired
    private UserRepository userRepository;

    @Transactional   //Dam bao khong bi loi database khi them du lieu vao
    public AuthRegisterResponse register(AuthRegisterRequest data)
    {
        Users user = userRepository.findByEmail(data.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));
        if (user != null && user.getActive())
            throw new BadRequestException("User has been exists");
        if (user != null && !user.getActive())
        {
            //Tao token va gui lai email verify
            //Tao jwt token ????
            return new AuthRegisterResponse(null , "123");
        }

        Users newUser = new Users();
        newUser.setEmail(data.getEmail());
        newUser.setPassword(data.getPassword());
        newUser.setActive(false);
//        userRepository.save(newUser); //Them user vao databaase
        //Token moi
        //Gui lai du lieu ve ben kia
        return new AuthRegisterResponse(new UserResponse(null , newUser.getEmail() , newUser.getCreatedAt(), false), "123");
    }
}
