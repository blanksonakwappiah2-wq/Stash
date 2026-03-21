package com.quickbite.backend;

import com.quickbite.backend.entities.DeliveryOption;
import com.quickbite.backend.entities.Restaurant;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.entities.UserRole;
import com.quickbite.backend.repositories.DeliveryOptionRepository;
import com.quickbite.backend.repositories.RestaurantRepository;
import com.quickbite.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BackendIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private DeliveryOptionRepository deliveryOptionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        userRepository.deleteAll();
        restaurantRepository.deleteAll();
        deliveryOptionRepository.deleteAll();
    }

    @Test
    void testContextLoads() {
        assertNotNull(userRepository);
        assertNotNull(restaurantRepository);
        assertNotNull(deliveryOptionRepository);
        assertNotNull(passwordEncoder);
    }

    @Test
    void testUserCreation() {
        User user = new User();
        user.setName("Integration Test User");
        user.setEmail("integration@test.com");
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setRole(UserRole.CUSTOMER);

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("Integration Test User", saved.getName());
        assertTrue(passwordEncoder.matches("Password123", saved.getPassword()));
    }

    @Test
    void testUserFindByEmail() {
        User user = new User();
        user.setName("Find By Email Test");
        user.setEmail("findbylemail@test.com");
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setRole(UserRole.CUSTOMER);

        userRepository.save(user);

        User found = userRepository.findByEmail("findbylemail@test.com");

        assertNotNull(found);
        assertEquals("Find By Email Test", found.getName());
    }

    @Test
    void testUserRoles() {
        User customer = new User();
        customer.setName("Customer");
        customer.setEmail("customer@test.com");
        customer.setPassword(passwordEncoder.encode("Password123"));
        customer.setRole(UserRole.CUSTOMER);

        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner.setPassword(passwordEncoder.encode("Password123"));
        owner.setRole(UserRole.RESTAURANT_OWNER);

        User agent = new User();
        agent.setName("Agent");
        agent.setEmail("agent@test.com");
        agent.setPassword(passwordEncoder.encode("Password123"));
        agent.setRole(UserRole.DELIVERY_AGENT);

        User manager = new User();
        manager.setName("Manager");
        manager.setEmail("manager@test.com");
        manager.setPassword(passwordEncoder.encode("Password123"));
        manager.setRole(UserRole.MANAGER);

        userRepository.saveAll(List.of(customer, owner, agent, manager));

        List<User> customers = userRepository.findByRole(UserRole.CUSTOMER);
        assertEquals(1, customers.size());

        List<User> owners = userRepository.findByRole(UserRole.RESTAURANT_OWNER);
        assertEquals(1, owners.size());
    }

    @Test
    void testRestaurantCreation() {
        User owner = new User();
        owner.setName("Restaurant Owner");
        owner.setEmail("restaurant.owner@test.com");
        owner.setPassword(passwordEncoder.encode("Password123"));
        owner.setRole(UserRole.RESTAURANT_OWNER);
        userRepository.save(owner);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Integration Test Restaurant");
        restaurant.setAddress("123 Test Street");
        restaurant.setContact("555-TEST");
        restaurant.setCategory("Italian");
        restaurant.setWebsite("https://test-restaurant.com");
        restaurant.setOwner(owner);

        Restaurant saved = restaurantRepository.save(restaurant);

        assertNotNull(saved.getId());
        assertEquals("Integration Test Restaurant", saved.getName());
        assertEquals(owner.getId(), saved.getOwner().getId());
    }

    @Test
    void testDeliveryOptionCreation() {
        DeliveryOption option = new DeliveryOption();
        option.setMethod("standard");
        option.setCategory("Food");
        option.setBaseFee(2.99);
        option.setPerKmFee(0.50);
        option.setEstimatedTime(45);

        DeliveryOption saved = deliveryOptionRepository.save(option);

        assertNotNull(saved.getId());
        assertEquals("standard", saved.getMethod());
        assertEquals(2.99, saved.getBaseFee());
    }

    @Test
    void testRestaurantFindByOwner() {
        User owner = new User();
        owner.setName("Test Owner");
        owner.setEmail("test.owner@test.com");
        owner.setPassword(passwordEncoder.encode("Password123"));
        owner.setRole(UserRole.RESTAURANT_OWNER);
        userRepository.save(owner);

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Owner's Restaurant");
        restaurant.setAddress("456 Owner Ave");
        restaurant.setContact("555-OWNR");
        restaurant.setCategory("American");
        restaurant.setOwner(owner);
        restaurantRepository.save(restaurant);

        List<Restaurant> found = restaurantRepository.findAll();
        assertEquals(1, found.size());
        assertEquals(owner.getId(), found.get(0).getOwner().getId());
    }

    @Test
    void testUniqueEmailConstraint() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("duplicate@test.com");
        user1.setPassword(passwordEncoder.encode("Password123"));
        user1.setRole(UserRole.CUSTOMER);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("duplicate@test.com");
        user2.setPassword(passwordEncoder.encode("Password456"));
        user2.setRole(UserRole.CUSTOMER);

        userRepository.save(user1);

        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
        });
    }
}
