package com.quickbite.backend.services;

import com.quickbite.backend.entities.User;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService.setPasswordEncoder(passwordEncoder);
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("Test User");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Another User");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testGetUserById_Found() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> found = userService.getUserById(1L);

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getName());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> found = userService.getUserById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    void testRegister_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.register("Test User", "test@example.com", "Password123", "CUSTOMER");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertTrue(passwordEncoder.matches("Password123", result.getPassword()));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(new User());

        assertThrows(RuntimeException.class, () -> {
            userService.register("Test User", "test@example.com", "Password123", "CUSTOMER");
        });
    }

    @Test
    void testRegister_InvalidEmail() {
        assertThrows(RuntimeException.class, () -> {
            userService.register("Test User", "invalid-email", "Password123", "CUSTOMER");
        });
    }

    @Test
    void testRegister_ShortPassword() {
        assertThrows(RuntimeException.class, () -> {
            userService.register("Test User", "test@example.com", "short", "CUSTOMER");
        });
    }

    @Test
    void testLogin_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Password123"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User result = userService.login("test@example.com", "Password123");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testLogin_Failure() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Password123"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User result = userService.login("test@example.com", "WrongPassword");

        assertNull(result);
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdateUser_Success() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setPassword(passwordEncoder.encode("OldPass123"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User updated = userService.updateUser(1L, "New Name", null, null);

        assertEquals("New Name", updated.getName());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, "New Name", null, null);
        });
    }
}
