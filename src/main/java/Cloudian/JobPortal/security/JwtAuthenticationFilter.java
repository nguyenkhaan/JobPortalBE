//Document: https://freedium-mirror.cfd/https://medium.com/%40sibinraziya/spring-boot-3-spring-security-6-jwt-authentication-and-authorization-e586bc186805?utm_source=chatgpt.com

package Cloudian.JobPortal.security;

import Cloudian.JobPortal.models.TokenType;
import Cloudian.JobPortal.modules.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
    @Autowired
    private final JwtService jwtService;
    @Autowired
    private final UserService userService;
    @Autowired
    private final UserDetailsServiceImpl userDetailImpService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService, UserDetailsServiceImpl userDetailImpService) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.userDetailImpService = userDetailImpService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException
    {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request , response);
            return;
        }
        //Extract Token
        String token = authHeader.substring(7);
        Boolean authResult = jwtService.validateToken(token , TokenType.ACCESS);
        if (!authResult) {
            filterChain.doFilter(request , response);
            return;
        }
        String email = jwtService.extractEmail(token , TokenType.ACCESS);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            //Get User Detail information
            UserDetails userDetails = userDetailImpService.loadUserByUsername(email);
            //Generate Authentication Object
            UsernamePasswordAuthenticationToken authObject = new UsernamePasswordAuthenticationToken(
                    userDetails , null  , userDetails.getAuthorities()
            );
            //Give it to Security Context
            SecurityContextHolder.getContext().setAuthentication(authObject);
        }
    }
}
