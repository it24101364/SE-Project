package com.example.webapp.Config;

import com.example.webapp.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);
        auth.authenticationProvider(authProvider());
        return auth.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF Handler for Thymeleaf
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .authorizeHttpRequests(auth -> auth
                        // Public URLs - allow access without authentication
                        .requestMatchers(
                                "/", "/login", "/register", "/verify-otp",
                                "/forgot-password", "/reset-password",
                                "/admin-login", "/access-denied",
                                "/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/favicon.ico"
                        ).permitAll()  // ✅ These URLs are accessible without login

                        // Admin URLs (require admin roles) - FIXED: Added MARKETING_MANAGER
                        .requestMatchers("/admin/**").hasAnyAuthority(
                                "SUPER_ADMIN", "USER_MANAGER", "STOCK_MANAGER",
                                "SALES_MANAGER", "ORDER_MANAGER", "MARKETING_MANAGER"
                        )

                        // Newsletter endpoints - FIXED: Added MARKETING_MANAGER
                        .requestMatchers("/admin/newsletter/**").hasAnyAuthority(
                                "SUPER_ADMIN", "MARKETING_MANAGER"
                        )

                        // Product, cart, checkout, order URLs require authentication
                        .requestMatchers("/products/**", "/cart/**", "/checkout/**", "/order/**").authenticated()

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )

                // Single form login configuration with CUSTOM success handler
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(customAuthenticationSuccessHandler()) // ✅ Use custom handler
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // Exception handling for access denied
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                )

                // Logout config
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                // Session management
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                )

                // CSRF config
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                        .csrfTokenRequestHandler(requestHandler)
                );

        return http.build();
    }

    // ✅ Custom Authentication Success Handler - FIXED: Added MARKETING_MANAGER
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                org.springframework.security.core.Authentication authentication)
                    throws IOException, ServletException {

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                // Check if user has any admin role - FIXED: Added MARKETING_MANAGER
                boolean isAdmin = authorities.stream()
                        .anyMatch(grantedAuthority -> {
                            String authority = grantedAuthority.getAuthority();
                            return authority.equals("SUPER_ADMIN") ||
                                    authority.equals("USER_MANAGER") ||
                                    authority.equals("STOCK_MANAGER") ||
                                    authority.equals("SALES_MANAGER") ||
                                    authority.equals("ORDER_MANAGER") ||
                                    authority.equals("MARKETING_MANAGER"); // ✅ Added this line
                        });

                if (isAdmin) {
                    response.sendRedirect("/admin/dashboard");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }
}