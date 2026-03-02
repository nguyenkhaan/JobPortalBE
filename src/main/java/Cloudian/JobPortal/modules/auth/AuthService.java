package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.commons.constants.TokenConstants;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.models.Token;
import Cloudian.JobPortal.models.TokenType;
import Cloudian.JobPortal.models.Users;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterRequest;
import Cloudian.JobPortal.modules.auth.dto.AuthRegisterResponse;
import Cloudian.JobPortal.modules.token.TokenRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import Cloudian.JobPortal.modules.user.dto.UserResponse;

import Cloudian.JobPortal.security.JwtService;
import Cloudian.JobPortal.security.TokenBody;
import Cloudian.JobPortal.utilis.SHA256Hashing;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class AuthService
{
    //User Repository
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JwtService jwtService;
    @org.springframework.beans.factory.annotation.Autowired(required=true)
    private PasswordEncoder passwordEncoder;
    @Transactional   //Dam bao khong bi loi database khi them du lieu vao
    public AuthRegisterResponse register(AuthRegisterRequest data)
    {
        //Tien hanh thuc hien viec dang ky
        Users user = userRepository.findByEmail(data.getEmail()).orElse(null);
        if (user != null && user.getActive())
        {
            throw new BadRequestException("User Has Exists");
        }
        if (user == null)
        {
            //Tien hanh tao user moi -> Gan lai bien user de su dung
            String hashPassword = passwordEncoder.encode(data.getPassword());
            user = Users.builder()
                    .email(data.getEmail())
                    .password(hashPassword)
                    .active(false)
                    .build();
            userRepository.save(user);
        }
        Long id = user.getId();
        Token verifiedToken = tokenRepository.getByUsersIDAndType(id , TokenType.REGISTER).orElse(null);
        if (verifiedToken != null)
            tokenRepository.deleteById(verifiedToken.getId());
            //Tien hanh tao token moi
        String token = jwtService.generateToken(new TokenBody(user.getEmail() , user.getId() , null , TokenType.REGISTER) , TokenType.REGISTER);
            //Luu Token vao database
        String hashedToken = SHA256Hashing.generateSHA256Hash(token);
        Token newToken = Token.builder()
                .token(hashedToken)
                .type(TokenType.REGISTER)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(TokenConstants.VERIFY_EMAIL_LIVE_TIME)))
                .usedAt(null)
                .usersID(user.getId())
                .build();
        newToken.setToken(hashedToken);
        tokenRepository.save(newToken);
        return new AuthRegisterResponse(
                new UserResponse(user.getId() , user.getEmail() , user.getCreatedAt() , user.getActive())
                ,token
        );
        //Neu user la null -> Tien hanh tap user moi
        //Tao token va gui ve cho nguoi dung
    }
    @Transactional
    public Boolean authRegisterVerify(String token)
    {
        try
        {
            Boolean validateResult = jwtService.validateToken(token , TokenType.REGISTER);
            if (validateResult)
            {
                String email = jwtService.extractEmail(token , TokenType.REGISTER);
                String purpose = jwtService.extractPurpose(token , TokenType.REGISTER);
                //Verify the token purpose
                if (!purpose.equals(TokenType.REGISTER.name()))
                    return false;
                //Find owner token
                Users user = userRepository.findByEmail(email).orElse(null);

                if (user == null)
                    throw new BadRequestException("User Has Not Registered");

                Token storedToken = tokenRepository.getByUsersIDAndType(user.getId() , TokenType.REGISTER)
                        .orElseThrow(() -> new BadRequestException("Token Not Found"));
                //Valiate the hash token
                System.out.println(SHA256Hashing.generateSHA256Hash(token));
                if (!(SHA256Hashing.verifyDataIntegrity(storedToken.getToken() , token)))
                    return false;
                //Kiem tra thoi han cua Token a Purpose cua token
                if (LocalDateTime.now().isAfter(storedToken.getExpiresAt())) {
                    throw new BadRequestException("Token expired");
                }
                //danh dau la token da duoc su dung
                storedToken.setUsedAt(LocalDateTime.now());
                tokenRepository.save(storedToken);
                user.setActive(true);
                userRepository.save(user);
                return true;
            }
            else throw new BadRequestException("Token Is Invalid");

        }
        catch (BadRequestException e)
        {
            throw e;
        }
        catch (RuntimeException e) {
            throw  e;
        }
    }
    @Transactional
    public void login()
    {

    }
}
