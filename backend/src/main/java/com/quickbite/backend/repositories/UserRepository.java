package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByEmailAndPassword(String email, String password);
    List<User> findByRole(User.UserRole role);
}