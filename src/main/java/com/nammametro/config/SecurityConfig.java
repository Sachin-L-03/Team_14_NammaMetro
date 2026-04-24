package com.nammametro.config;

import com.nammametro.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration.
 *
 * SRP: This class has one responsibility — defining the security filter chain,
 *      role-based access rules, password encoder, and authentication provider.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Defines the security filter chain with:
     * - Stateless session management (JWT-based)
     * - Role-based URL access control
     * - JWT filter inserted before UsernamePasswordAuthenticationFilter
     * - Custom login/logout page redirects
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disabled for JWT-based auth

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // Public pages — accessible without authentication
                .requestMatchers(
                    "/", "/auth/**", "/css/**", "/js/**", "/images/**",
                    "/error", "/error/**", "/favicon.ico",
                    "/schedules/public/**"
                ).permitAll()

                // Role-based dashboard access control
                .requestMatchers("/passenger/**").hasRole("PASSENGER")
                .requestMatchers("/operator/**").hasRole("OPERATOR")
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // All other pages require authentication
                .anyRequest().authenticated()
            )

            // Add JWT filter before the default username/password filter
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class)

            // Handle access denied (no permission) — redirect to login
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/auth/login?error=Please+log+in+first")
                )
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect("/auth/login?error=Access+denied")
                )
            );

        return http.build();
    }

    /**
     * BCrypt password encoder — used for hashing passwords on registration
     * and verifying them on login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider wired to our UserDetailsService + BCrypt encoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean for use in AuthService.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
