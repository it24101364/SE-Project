package com.example.webapp.service;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("No user found with email: " + email);
        }

        // Create a UserDetails object
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())  // must match form input name
                .password(user.getPassword())
                .authorities(Collections.emptyList()) // no roles for now
                .build();
    }
}
