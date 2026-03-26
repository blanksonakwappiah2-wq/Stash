package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.User;
import com.quickbite.backend.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByEmailAndPassword(String email, String password);

    List<User> findByRole(UserRole role);

    List<User> findByRoleAndOnline(UserRole role, Boolean online);
}