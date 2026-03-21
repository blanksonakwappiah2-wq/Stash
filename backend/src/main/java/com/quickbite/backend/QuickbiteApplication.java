package com.quickbite.backend;

import com.quickbite.backend.entities.DeliveryOption;
import com.quickbite.backend.entities.MenuItem;
import com.quickbite.backend.entities.Restaurant;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.entities.UserRole;
import com.quickbite.backend.repositories.DeliveryOptionRepository;
import com.quickbite.backend.repositories.MenuItemRepository;
import com.quickbite.backend.repositories.RestaurantRepository;
import com.quickbite.backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableCaching
public class QuickbiteApplication {
    public static void main(String[] args) {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl != null && dbUrl.startsWith("postgres://")) {
            String cleanUrl = dbUrl.substring(11);
            String[] userPassAndHostAndDb = cleanUrl.split("@");
            String[] userPass = userPassAndHostAndDb[0].split(":");
            String hostPortDb = userPassAndHostAndDb[1];

            System.setProperty("spring.datasource.url", "jdbc:postgresql://" + hostPortDb);
            System.setProperty("spring.datasource.username", userPass[0]);
            if (userPass.length > 1) {
                System.setProperty("spring.datasource.password", userPass[1]);
            }
            System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");

            // Also override the old DB_HOST variables in case the user has manual overrides
            // causing issues
            System.clearProperty("DB_HOST");
        }

        SpringApplication.run(QuickbiteApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(DeliveryOptionRepository deliveryOptionRepository,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository) {
        return args -> {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            if (userRepository.findByEmail("manager@quickbite.com") == null) {
                User manager = new User();
                manager.setName("Admin Manager");
                manager.setEmail("manager@quickbite.com");
                manager.setPassword(passwordEncoder.encode("Manager123"));
                manager.setRole(UserRole.MANAGER);
                userRepository.save(manager);
            }

            if (userRepository.findByEmail("customer@quickbite.com") == null) {
                User customer = new User();
                customer.setName("Test Customer");
                customer.setEmail("customer@quickbite.com");
                customer.setPassword(passwordEncoder.encode("Customer123"));
                customer.setRole(UserRole.CUSTOMER);
                userRepository.save(customer);
            }

            if (userRepository.findByEmail("owner@quickbite.com") == null) {
                User owner = new User();
                owner.setName("Restaurant Owner");
                owner.setEmail("owner@quickbite.com");
                owner.setPassword(passwordEncoder.encode("Owner123"));
                owner.setRole(UserRole.RESTAURANT_OWNER);
                userRepository.save(owner);
            }

            if (userRepository.findByEmail("agent@quickbite.com") == null) {
                User agent = new User();
                agent.setName("Delivery Agent");
                agent.setEmail("agent@quickbite.com");
                agent.setPassword(passwordEncoder.encode("Agent123"));
                agent.setRole(UserRole.DELIVERY_AGENT);
                userRepository.save(agent);
            }

            if (restaurantRepository.count() == 0) {
                User owner = userRepository.findByEmail("owner@quickbite.com");

                Restaurant pizza = new Restaurant();
                pizza.setName("Pepperoni Palace");
                pizza.setCategory("Pizza");
                pizza.setAddress("123 Pizza St");
                pizza.setContact("555-PIZZA");
                pizza.setWebsite("https://pepperonipalace.com");
                pizza.setOwner(owner);
                restaurantRepository.save(pizza);

                // Seed Menu Items for Pizza
                MenuItem m1 = new MenuItem();
                m1.setName("Margherita Pizza");
                m1.setPrice(12.99);
                m1.setDescription("Fresh basil, mozzarella, and tomato sauce.");
                m1.setRestaurant(pizza);
                menuItemRepository.save(m1);

                Restaurant burger = new Restaurant();
                burger.setName("Burger Heaven");
                burger.setCategory("Burgers");
                burger.setAddress("456 Burger Ave");
                burger.setContact("555-BURGER");
                burger.setWebsite("https://burgerheaven.com");
                restaurantRepository.save(burger);

                Restaurant sushi = new Restaurant();
                sushi.setName("Sakura Sushi");
                sushi.setCategory("Sushi");
                sushi.setAddress("789 Sushi Rd");
                sushi.setContact("555-SUSHI");
                sushi.setWebsite("https://sakurasushi.com");
                restaurantRepository.save(sushi);

                Restaurant chinese = new Restaurant();
                chinese.setName("Dragon Wok");
                chinese.setCategory("Chinese");
                chinese.setAddress("321 Wok Way");
                chinese.setContact("555-WOK");
                chinese.setWebsite("https://dragonwok.com");
                restaurantRepository.save(chinese);

                Restaurant dessert = new Restaurant();
                dessert.setName("Sweet Treats");
                dessert.setCategory("Desserts");
                dessert.setAddress("654 Sugar Ln");
                dessert.setContact("555-SWEET");
                dessert.setWebsite("https://sweettreats.com");
                restaurantRepository.save(dessert);
            }

            if (deliveryOptionRepository.count() == 0) {
                DeliveryOption standard = new DeliveryOption();
                standard.setMethod("standard");
                standard.setCategory("Food");
                standard.setBaseFee(2.99);
                standard.setPerKmFee(0.50);
                standard.setEstimatedTime(45);
                deliveryOptionRepository.save(standard);

                DeliveryOption express = new DeliveryOption();
                express.setMethod("express");
                express.setCategory("Food/Express");
                express.setBaseFee(4.99);
                express.setPerKmFee(0.75);
                express.setEstimatedTime(25);
                deliveryOptionRepository.save(express);

                DeliveryOption drone = new DeliveryOption();
                drone.setMethod("drone");
                drone.setCategory("Specialty");
                drone.setBaseFee(6.99);
                drone.setPerKmFee(1.00);
                drone.setEstimatedTime(15);
                deliveryOptionRepository.save(drone);
            }
        };
    }
}