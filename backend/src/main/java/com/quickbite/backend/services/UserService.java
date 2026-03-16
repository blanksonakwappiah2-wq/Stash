package com.quickbite.backend.services;

import com.quickbite.backend.entities.User;
import com.quickbite.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

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
        return userRepository.findByEmailAndPassword(email, password);
    }

    public User register(String name, String email, String password, String role) {
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // In real app, hash password
        user.setRole(User.UserRole.valueOf(role));
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}