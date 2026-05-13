package Cloudian.JobPortal.modules.auth;

import Cloudian.JobPortal.commons.constants.TokenConstants;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.auth.dto.*;
import Cloudian.JobPortal.modules.role.UserRoleRepository;
import Cloudian.JobPortal.modules.token.TokenRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import Cloudian.JobPortal.modules.user.dto.UserResponse;

import Cloudian.JobPortal.security.JwtService;
import Cloudian.JobPortal.security.TokenBody;
import Cloudian.JobPortal.utilis.SHA256Hashing;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
    @Autowired
    private UserRoleRepository userRoleRepository;
    @org.springframework.beans.factory.annotation.Autowired(required=true)
    private PasswordEncoder passwordEncoder;
    @Transactional   //Dam bao khong bi loi database khi them du lieu vao
    public AuthRegisterResponse register(AuthRegisterRequest data)
    {
        //Tien hanh thuc hien viec dang ky
        User user = userRepository.findByEmail(data.getEmail()).orElse(null);
        if (user != null && user.getActive())
        {
            throw new BadRequestException("User Has Exists");
        }
        if (user == null)
        {
            //Tien hanh tao user moi -> Gan lai bien user de su dung
            String hashPassword = passwordEncoder.encode(data.getPassword());

            user = User.builder()
                    .email(data.getEmail())
                    .password(hashPassword)
                    .active(false)
                    .build();
            UserRole userRole = new UserRole();
            //n - 1
            userRole.setRole(Role.SEEKER);
            userRole.setUser(user);  //Co getter - setter nen co the hoan thien
            //1 - n
            //add user roles
            user.getUserRoleList().add(userRole);


            userRepository.save(user);
            userRoleRepository.save(userRole);


        }
        Long id = user.getId();
        Token verifiedToken = tokenRepository.findByUserIdAndType(id , TokenType.REGISTER).orElse(null);
        if (verifiedToken != null)
            tokenRepository.deleteById(verifiedToken.getId());
            //Tien hanh tao token moi
        String token = jwtService.generateToken(new TokenBody(user.getEmail() , user.getId() , null , TokenType.REGISTER , null) , TokenType.REGISTER);
            //Luu Token vao database
        String hashedToken = SHA256Hashing.generateSHA256Hash(token);
        Token newToken = Token.builder()
                .token(hashedToken)
                .type(TokenType.REGISTER)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(TokenConstants.VERIFY_EMAIL_LIVE_TIME)))
                .usedAt(null)
                .userId(user.getId())
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
        Boolean validateResult = jwtService.validateToken(token , TokenType.REGISTER);
        if (validateResult)
        {
            String email = jwtService.extractEmail(token , TokenType.REGISTER);
            String purpose = jwtService.extractPurpose(token , TokenType.REGISTER);
            //Verify the token purpose
            if (!purpose.equals(TokenType.REGISTER.name()))
                return false;
            //Find owner token
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null)
                throw new BadRequestException("User Has Not Registered");

            Token storedToken = tokenRepository.findByUserIdAndType(user.getId() , TokenType.REGISTER)
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
    @Transactional
    public AuthLoginResponse login(AuthLoginRequest data)
    {
        String email = data.getEmail();
        User user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null || !user.getActive())
            throw new UnauthorizedException("Unauthorized User");
        //Assign Roles
        ArrayList<Role> assignedRoles =
                user.getUserRoleList()
                        .stream()
                        .map(UserRole::getRole)
                        .collect(Collectors.toCollection(ArrayList::new));
        //Kiem tra them password co dung hay khong
        String accessToken = jwtService.generateToken(
                new TokenBody(user.getEmail() ,
                        user.getId() ,
                        assignedRoles,
                        TokenType.ACCESS , null),
                TokenType.ACCESS
        );
        String refreshToken = jwtService.generateToken(
                new TokenBody(user.getEmail() , user.getId() , assignedRoles , TokenType.REFRESH , Provider.LOCAL.name()),
                TokenType.REFRESH 
        );
        //Xoa va luu lai token sau
        String password = data.getPassword();
        if (!passwordEncoder.matches(password , user.getPassword()))
            throw new BadRequestException("Wrong Password");
        //Xoa va luu lai token sau
        //Delete Access Token
        tokenRepository.findByUserIdAndType(user.getId(), TokenType.ACCESS).ifPresent(storedAccessToken -> tokenRepository.delete(storedAccessToken));
        //Delete Refresh Token
        tokenRepository.findByUserIdAndType(user.getId(), TokenType.REFRESH).ifPresent(storeRefreshToken -> tokenRepository.delete(storeRefreshToken));
        AuthLoginResponse response = AuthLoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(email)
                .id(user.getId())
                .build();
        return response;
    }
    //Response: token (to reset), email,
    @Transactional
    public ResetPasswordResponse resetPassword(String email)
    {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new BadRequestException("User not found")
        );
        String verifyToken = jwtService.generateToken(
                new TokenBody(user.getEmail() , user.getId() , null , null , null), TokenType.RESET_PASSWORD
        );
        String hashedToken = SHA256Hashing.generateSHA256Hash(verifyToken);
        Token tk = Token.builder()
                .expiresAt(LocalDateTime.now().plusSeconds(TokenConstants.RESET_PASSWORD_LIVE_TIME / 1000))
                .token(hashedToken)
                .userId(user.getId())
                .usedAt(null)
                .type(TokenType.RESET_PASSWORD)
                .build();
        //Remove the old token
        tokenRepository.findByUserIdAndType(user.getId(), TokenType.RESET_PASSWORD).ifPresent(storedToken -> tokenRepository.delete(storedToken));

        //Save the token to the database
        tokenRepository.save(tk);
        return new ResetPasswordResponse(
                user.getEmail() , verifyToken
        );
    }
    //Phai co transaction thi chung ta tu dong update, luc nay JPA se tu dong .save() luon, chung ta do phai code
    //COn khong co tracsactional thi store: .save(), update: save(), delete: .delete()
    @Transactional
    public boolean verifyResetPasswordToken(String token , String password)
    {
        String hashedToken = SHA256Hashing.generateSHA256Hash(token);
        Token storedToken = tokenRepository.findByToken(hashedToken).orElse(null);
        if (storedToken == null)
            throw new BadRequestException("Token not found");
        if (storedToken.getUsedAt() != null || storedToken.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new BadRequestException("Token is invalid");
        String email = jwtService.extractEmail(token , TokenType.RESET_PASSWORD);
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            throw new BadRequestException("Email not found");
        String hashedPassword = passwordEncoder.encode(password);
        System.out.println(password + "   " + hashedPassword);
        user.setPassword(hashedPassword);
        storedToken.setUsedAt(LocalDateTime.now());
        storedToken.setUserId(user.getId());
        return true;
    }
    @Transactional //Da test: Neu khong co cai nay thi khi update bang ham set, chugn ta can phai ch userRepo.save(), con neu co cai nay thi an toan hon va khong can userRepo.save() lai
    public boolean resetEmail(String email, String password , String updateEmail)
    {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            throw new BadRequestException("User not found");
        if (passwordEncoder.matches(password , user.getPassword()))
        {
            user.setEmail(updateEmail);
            return true;
        }
        throw new BadRequestException("Wrong password");
    }
}
