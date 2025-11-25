package com.example.webapp;

import com.example.webapp.model.Admin;
import com.example.webapp.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@SpringBootApplication
public class WebappApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebappApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(AdminRepository repo, PasswordEncoder encoder) {
        return args -> {

            if (repo.findByEmail("mainadmin@example.com") == null) {
                Admin admin = new Admin();
                admin.setEmail("mainadmin@example.com");
                admin.setPassword(encoder.encode("main123"));
                admin.setRoles(Set.of("SUPER_ADMIN"));
                repo.save(admin);

                System.out.println("SUPER ADMIN CREATED: mainadmin@example.com / main123");
            }
        };
    }


}
