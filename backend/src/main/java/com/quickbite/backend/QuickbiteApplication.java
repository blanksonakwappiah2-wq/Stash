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
        SpringApplication.run(QuickbiteApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(DeliveryOptionRepository deliveryOptionRepository,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            org.springframework.core.env.Environment env) {
        return args -> {
            boolean shouldSeed = env.getProperty("quickbite.seed-db", Boolean.class, true);
            if (!shouldSeed) return;

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            if (userRepository.findByEmail("manager@quickbite.com") == null) {
                User manager = new User();
                manager.setName("Admin Manager");
                manager.setEmail("manager@quickbite.com");
                manager.setPassword(passwordEncoder.encode("Manager123"));
                manager.setRole(UserRole.MANAGER);
                userRepository.save(manager);
                System.out.println("Seeded Manager account.");
            }

            if (userRepository.findByEmail("customer@quickbite.com") == null) {
                User customer = new User();
                customer.setName("Test Customer");
                customer.setEmail("customer@quickbite.com");
                customer.setPassword(passwordEncoder.encode("Customer123"));
                customer.setRole(UserRole.CUSTOMER);
                userRepository.save(customer);
                System.out.println("Seeded Customer account.");
            }

            if (restaurantRepository.count() == 0) {
                User owner = userRepository.findByEmail("owner@quickbite.com");
                if (owner == null) {
                    owner = new User();
                    owner.setName("Restaurant Owner");
                    owner.setEmail("owner@quickbite.com");
                    owner.setPassword(passwordEncoder.encode("Owner123"));
                    owner.setRole(UserRole.RESTAURANT_OWNER);
                    userRepository.save(owner);
                    System.out.println("Seeded Owner account.");
                }

                Restaurant pizza = new Restaurant();
                pizza.setName("Grandma's Kitchen");
                pizza.setCategory("Italian");
                pizza.setAddress("123 Artisan Way, Roma");
                pizza.setContact("555-KITCHEN");
                pizza.setImageUrl("https://i.pinimg.com/736x/1e/95/a3/1e95a3d36f534d755e4b0a82934c9b11.jpg");
                pizza.setOwner(owner);
                restaurantRepository.save(pizza);

                Restaurant burgers = new Restaurant();
                burgers.setName("Burger House");
                burgers.setCategory("Fast Food");
                burgers.setAddress("456 Grill St, Metro");
                burgers.setContact("555-BURGER");
                burgers.setImageUrl("https://i.pinimg.com/736x/21/df/76/21df767439167df3c4c9b1f618a56249.jpg");
                restaurantRepository.save(burgers);

                Restaurant sushi = new Restaurant();
                sushi.setName("Sushi Master");
                sushi.setCategory("Japanese");
                sushi.setAddress("789 Sakura Blvd, Neo");
                sushi.setContact("555-SUSHI");
                sushi.setImageUrl("https://i.pinimg.com/736x/09/a4/09/09a4097e034e64a13d7e6f6f1a8e1e81.jpg");
                restaurantRepository.save(sushi);

                MenuItem m1 = new MenuItem();
                m1.setName("Classic Margherita");
                m1.setPrice(12.99);
                m1.setRestaurant(pizza);
                menuItemRepository.save(m1);
                System.out.println("Seeded Restaurant and Menu.");
            }

            if (deliveryOptionRepository.count() == 0) {
                DeliveryOption standard = new DeliveryOption();
                standard.setMethod("standard");
                standard.setCategory("Food");
                standard.setBaseFee(2.99);
                standard.setPerKmFee(0.50);
                standard.setEstimatedTime(45);
                deliveryOptionRepository.save(standard);
                System.out.println("Seeded Delivery Options.");
            }
        };
    }
}