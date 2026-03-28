package com.quickbite.backend.controllers;

import com.quickbite.backend.dto.AuthResponse;
import com.quickbite.backend.dto.LoginRequest;
import com.quickbite.backend.dto.RegisterRequest;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.repositories.UserRepository;
import com.quickbite.backend.security.JwtUtil;
import com.quickbite.backend.services.UserService;
import com.quickbite.backend.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER')")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_AGENT')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id).orElse(null);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER')")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and password are required"));
        }

        try {
            User user = userService.findByEmail(loginRequest.getEmail());
            if (user != null) {
                if (!user.isEmailVerified()) {
                    return ResponseEntity.status(403)
                            .body(Map.of("message", "Email not verified. Please verify your email first."));
                }

                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

                UserDetails userDetails = org.springframework.security.core.userdetails.User
                        .builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build();

                String token = jwtUtil.generateToken(userDetails);

                AuthResponse response = new AuthResponse(
                        token,
                        user.getEmail(),
                        user.getRole().name(),
                        user.getName(),
                        user.getId());

                // Send login notification asynchronously
                emailService.sendLoginNotification(user);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Invalid email or password"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid email or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.register(
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getRole());

            // Set additional fields if provided
            if (registerRequest.getAddress() != null) {
                user.setAddress(registerRequest.getAddress());
            }
            if (registerRequest.getPhone() != null) {
                user.setPhone(registerRequest.getPhone());
            }
            if (registerRequest.getLatitude() != null) {
                user.setLatitude(registerRequest.getLatitude());
            }
            if (registerRequest.getLongitude() != null) {
                user.setLongitude(registerRequest.getLongitude());
            }
            user = userRepository.save(user);

            // Generate token for newly registered user
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();

            String token = jwtUtil.generateToken(userDetails);

            AuthResponse response = new AuthResponse(
                    token,
                    user.getEmail(),
                    user.getRole().name(),
                    user.getName(),
                    user.getId());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (userService.verifyEmail(email, code)) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully!"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification code."));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_AGENT')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RegisterRequest userData) {
        try {
            User user = userService.updateUser(id, userData.getName(), userData.getEmail(), userData.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
            String email = jwtUtil.extractUsername(token);
            User user = userService.findByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid token"));
        }
    }
}