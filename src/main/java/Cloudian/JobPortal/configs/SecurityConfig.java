package Cloudian.JobPortal.configs;

import Cloudian.JobPortal.security.JwtAuthenticationFilter;
import Cloudian.JobPortal.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ADDED CORS CONFIGURATION HERE
                .cors(cors -> {})

                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").hasRole("ADMIN")
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/plans/**").permitAll()
                                .requestMatchers("/auth/refresh").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/auth/register").permitAll()
                                .requestMatchers("/health").permitAll()
                                .requestMatchers("/email").permitAll()
                                .requestMatchers("/auth/reset-password").permitAll()
                                .requestMatchers("/auth/verify-reset-password").permitAll()
                                .requestMatchers("/docs/**", "/docs").permitAll()
                                .requestMatchers(HttpMethod.GET , "/jobpost", "/jobpost/**").permitAll()
                                .requestMatchers("/scalar/**").permitAll()
                                .requestMatchers("/openapi.json", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/auth/verify").permitAll()
                                .requestMatchers("/auth/login").permitAll()
                                .requestMatchers("/test").permitAll()
                                .requestMatchers(HttpMethod.POST, "/payments/webhook").permitAll()
                                .anyRequest().authenticated()
                );

        // Fix H2 console
        http.headers(headers -> headers.frameOptions(frameOption -> frameOption.sameOrigin()));
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ADDED BEAN FOR CORS CONFIGURATION
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow your React frontend (localhost:5173). Add other domains if needed.
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
//
//        // Allow common HTTP methods
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
//
//        // Allow headers required for JWT and JSON requests
//        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token", "Origin", "Accept"));
//        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
//
//        // Allow credentials (cookies, authorization headers)
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // Apply this configuration to all endpoints
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
}