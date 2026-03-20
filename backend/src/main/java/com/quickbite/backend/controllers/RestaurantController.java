package com.quickbite.backend.controllers;

import com.quickbite.backend.entities.Restaurant;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.services.RestaurantService;
import com.quickbite.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
@CrossOrigin(origins = "*")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.getAllRestaurants();
    }

    @PostMapping
    public Restaurant createRestaurant(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String address = (String) payload.get("address");
        String contact = (String) payload.get("contact");
        String website = (String) payload.get("website");
        
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setContact(contact);
        restaurant.setWebsite(website);
        restaurant.setCategory((String) payload.getOrDefault("category", "General"));

        // Handle Owner Creation if provided
        if (payload.containsKey("ownerName") && payload.containsKey("ownerEmail")) {
            String ownerName = (String) payload.get("ownerName");
            String ownerEmail = (String) payload.get("ownerEmail");
            String ownerPassword = (String) payload.get("ownerPassword");
            
            User owner = userService.register(ownerName, ownerEmail, ownerPassword, "RESTAURANT_OWNER");
            restaurant.setOwner(owner);
        } else if (payload.containsKey("owner")) {
            // Handle existing owner ID if passed as object
            Map<String, Object> ownerData = (Map<String, Object>) payload.get("owner");
            if (ownerData.containsKey("id")) {
                Long ownerId = ((Number) ownerData.get("id")).longValue();
                User existingOwner = userService.getUserById(ownerId).orElse(null);
                restaurant.setOwner(existingOwner);
            }
        }

        return restaurantService.createRestaurant(restaurant);
    }
}
