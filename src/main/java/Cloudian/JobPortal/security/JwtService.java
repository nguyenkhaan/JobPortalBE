package Cloudian.JobPortal.security;

import Cloudian.JobPortal.commons.constants.TokenConstants;
import Cloudian.JobPortal.models.Token;
import Cloudian.JobPortal.models.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService
{
    record TokenInformation(String secretKey , Long liveTime) {}
    Map<TokenType , TokenInformation> mapping = new HashMap<>();
    //Verify Email secret key
    @Value("${app.secret.verify.email}")
    String verifyEmailSecretKey;
    @Value("${app.secret.reset.password}")
    String resetPasswordSecretKey;
    @Value("${app.secret.access}")
    String accessSecretKey;
    @Value("${app.secret.refresh}")
    String refreshSecretKey;

    @PostConstruct
    //Util Method
    public void init()
    {
        mapping.put(TokenType.ACCESS, new TokenInformation(
                accessSecretKey,
                TokenConstants.ACCESS_LIVE_TIME));
        mapping.put(TokenType.REFRESH, new TokenInformation(
                refreshSecretKey,
                TokenConstants.REFRESH_LIVE_TIME));
        mapping.put(
                TokenType.REGISTER,
                new TokenInformation(verifyEmailSecretKey , TokenConstants.VERIFY_EMAIL_LIVE_TIME));
        mapping.put(
                TokenType.RESET_PASSWORD,
                new TokenInformation(resetPasswordSecretKey , TokenConstants.RESET_PASSWORD_LIVE_TIME));
        //reset email token
    }
    private SecretKey getSecretKey(TokenType type)
    {
        return Keys.hmacShaKeyFor(mapping.get(type).secretKey().getBytes());
    }
    //Generate Token
    public String generateToken(TokenBody payload , TokenType type)
    {
        //Truyen thong tin payload vao
        //Su dung 1 cai map de nhan thong tin
        HashMap<String , Object> claims = new HashMap<>();
        if (payload.getEmail() != null)
            claims.put("email" , payload.getEmail());
        if (payload.getRoles() != null)
            claims.put("roles" , payload.getRoles().stream().map(Enum::name).toList()); //Du lieu trong model la enum, jwt khong the chuyen doi cai nay
        if (payload.getId() != null)
            claims.put("id" , payload.getId());
        if (payload.getPurpose() != null)
            claims.put("purpose" , payload.getPurpose().name().toString());

        Date now = new Date();
        Date exp = new Date(System.currentTimeMillis() + mapping.get(type).liveTime());

        System.out.println("NOW: " + now);
        System.out.println("EXP: " + exp);

        String token = Jwts
                .builder()
                .subject(payload.getEmail())
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + mapping.get(type).liveTime()))
                .signWith(getSecretKey(type))
                .compact();
        return token;
    }
    //Validate Token
    public Boolean validateToken(String token , TokenType type)
    {
        String secretKey = mapping.get(type).secretKey();
        try
        {
            Jwts.parser()
                    .verifyWith(getSecretKey(type))
                    .build()
                    .parseSignedClaims(token);
            return true; //validate successfully
        }
        catch (Exception e) {
            return false;
        }
    }
    //Extract Token Information
    private Claims extractAllClaims(String token , TokenType type)
    {
        return Jwts.parser().
                verifyWith(getSecretKey(type))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public<T> T extractClaims(String token , TokenType type , Function<Claims , T> resolver)
    {
        Claims claims = extractAllClaims(token , type);
        return resolver.apply(claims);
    }
    //Some builtin extract function
    public String extractSubject(String token , TokenType type)
    {
        return extractClaims(token , type , Claims::getSubject);
    }
    public String extractEmail(String token , TokenType type)
    {
        return extractClaims(token , type, claims -> claims.get("email" , String.class));
    }
    public String extractPurpose(String token , TokenType type)
    {
        return extractClaims(token , type, claims -> claims.get("purpose" , String.class));
    }
    public Long extractId(String token , TokenType type)  //Lay ra id
    {
        return extractClaims(token , type , claims -> claims.get("id" , Long.class));
    }
}
