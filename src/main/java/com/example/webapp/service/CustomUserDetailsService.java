package com.example.webapp.service;

import com.example.webapp.model.User;
import com.example.webapp.model.Admin;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.repository.AdminRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First, try to find as Admin
        Admin admin = adminRepository.findByEmail(email);
        if (admin != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities(
                            admin.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority(role))
                                    .collect(Collectors.toList())
                    )
                    .build();
        }

        // If not admin, try to find as regular User
        User user = userRepository.findByEmail(email);
        if (user != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();
        }

        throw new UsernameNotFoundException("No user found with email: " + email);
    }
}