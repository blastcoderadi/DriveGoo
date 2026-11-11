package com.drivego.config;

import com.drivego.security.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring Security configuration.
 * Supports Google OAuth2 login and phone OTP session-based auth.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public pages (browse, booking steps 1-3, auth, info, static assets)
                .requestMatchers("/", "/vehicles", "/vehicles/**",
                                 "/booking/**",
                                 "/auth/**", "/css/**", "/js/**",
                                 "/images/**", "/about",
                                 "/h2-console/**", "/error").permitAll()
                // Admin portal
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                // Customer dashboard and history
                .requestMatchers("/customer/**").authenticated()
                .anyRequest().authenticated()
            )
            // Google OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("/auth/login?error=oauth")
            )
            // Logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Exception handling - redirect unauthenticated users to login or handle access denied
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/auth/login"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect("/auth/login?error=access_denied"))
            )
            // Allow H2 console frames in dev
            .headers(h -> h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }
}
