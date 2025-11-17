package com.example.webapp.repository;

import com.example.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username); // ✅ Added method
    User findByResetToken(String token); // New

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE %:key% OR LOWER(u.email) LIKE %:key%")
    List<User> searchUsers(@Param("key") String keyword);

}
