package Cloudian.JobPortal.filters;

import Cloudian.JobPortal.models.TokenType;
import Cloudian.JobPortal.security.JwtService;
import Cloudian.JobPortal.security.UserDetailsImpl;
import Cloudian.JobPortal.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthenticationJwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationJwtFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try
        {
            String jwt = parseJwt(request);
            if (jwt != null && jwtService.validateToken(jwt , TokenType.ACCESS))
            {
                String email = jwtService.extractEmail(jwt , TokenType.ACCESS);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authObject =
                        new UsernamePasswordAuthenticationToken(
                                userDetails , null , userDetails.getAuthorities()
                        );
                authObject.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authObject);
            }
            filterChain.doFilter(request , response);  //Cho Filter di tiep tuc
        }
        catch (Exception e)
        {
            logger.error("Cannot set user authentication: {}", e);
        }
    }
    private String parseJwt(HttpServletRequest request)
    {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth == null || !headerAuth.startsWith("Bearer "))
            return null;
        return headerAuth.substring(7);
    }
}
