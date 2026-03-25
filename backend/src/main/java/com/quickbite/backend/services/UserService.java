package com.quickbite.backend.services;

import com.quickbite.backend.entities.User;
import com.quickbite.backend.entities.UserRole;
import com.quickbite.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Setter for testing
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public User register(String name, String email, String password, String role) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (email == null || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("Valid email is required");
        }
        if (password == null || password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }

        UserRole targetRole;
        try {
            targetRole = UserRole.valueOf(role.toUpperCase());
        } catch (Exception e) {
            targetRole = UserRole.CUSTOMER;
        }


        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(targetRole);

        return userRepository.save(user);
    }

    public User updateUser(Long id, String name, String email, String password) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (name != null && !name.trim().isEmpty())
            user.setName(name);
        if (email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            User existing = userRepository.findByEmail(email);
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("Email already taken");
            }
            user.setEmail(email);
        }
        if (password != null && password.length() >= 8) {
            user.setPassword(passwordEncoder.encode(password));
        }

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}