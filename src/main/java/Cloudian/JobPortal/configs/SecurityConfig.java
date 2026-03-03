//Document for jwt security config (Very Easy but I don't understand the code a lot...
//Source from Medium
//https://freedium-mirror.cfd/https://medium.com/%40sibinraziya/spring-boot-3-spring-security-6-jwt-authentication-and-authorization-e586bc186805
package Cloudian.JobPortal.configs;

import Cloudian.JobPortal.filters.AuthenticationJwtFilter;
import Cloudian.JobPortal.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public AuthenticationJwtFilter authenticationJwtTokenFilter() {
        return new AuthenticationJwtFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean  //Password Encoder for hashing password
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").hasRole("ADMIN")  //Hoac co the dung annotation ben trong Jwt
                                .requestMatchers("/api/test/**").permitAll()
                                //hasRole("ADMIN") tu dong them tien to ROLE_ vao. => O Spring Security thi map thanh ROLE_, jwt luu ADMIN thoi cung duoc
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/auth/register").permitAll()
                                .requestMatchers("/auth/verify").permitAll()
                                .requestMatchers("/auth/login").permitAll()
                                .requestMatchers("/auth/reset-password").permitAll()
                                .requestMatchers("/auth/reset-email").permitAll()
                                .requestMatchers("/test").permitAll()
                                .requestMatchers("/auth/login").permitAll()
                                .anyRequest().authenticated()  //Ap dung cho route nao thi khai bao vao day
                );

        // Fix H2 console
        http.headers(headers -> headers.frameOptions(frameOption -> frameOption.sameOrigin()));
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}