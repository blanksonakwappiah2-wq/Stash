package com.quickbite.backend.controllers;

import com.quickbite.backend.dto.RestaurantDTO;
import com.quickbite.backend.dto.RestaurantRequest;
import com.quickbite.backend.entities.Restaurant;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.services.RestaurantService;
import com.quickbite.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantService.getAllRestaurants().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRestaurantById(@PathVariable Long id) {
        return restaurantService.getRestaurantById(id)
                .<ResponseEntity<?>>map(restaurant -> ResponseEntity.ok(convertToDTO(restaurant)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> createRestaurant(@RequestBody RestaurantRequest request) {
        try {
            Restaurant restaurant = new Restaurant();
            restaurant.setName(request.getName());
            restaurant.setAddress(request.getAddress());
            restaurant.setContact(request.getContact());
            restaurant.setWebsite(request.getWebsite());
            restaurant.setCategory(request.getCategory() != null ? request.getCategory() : "General");

            // Handle Owner
            if (request.getOwnerId() != null) {
                User existingOwner = userService.getUserById(request.getOwnerId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", request.getOwnerId()));
                restaurant.setOwner(existingOwner);
            }

            Restaurant created = restaurantService.createRestaurant(restaurant);
            return ResponseEntity.ok(convertToDTO(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESTAURANT_OWNER')")
    public ResponseEntity<?> updateRestaurant(@PathVariable Long id, @RequestBody RestaurantRequest request) {
        try {
            Restaurant existing = restaurantService.getRestaurantById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));

            if (request.getName() != null) {
                existing.setName(request.getName());
            }
            if (request.getAddress() != null) {
                existing.setAddress(request.getAddress());
            }
            if (request.getContact() != null) {
                existing.setContact(request.getContact());
            }
            if (request.getWebsite() != null) {
                existing.setWebsite(request.getWebsite());
            }
            if (request.getCategory() != null) {
                existing.setCategory(request.getCategory());
            }

            Restaurant updated = restaurantService.updateRestaurant(existing);
            return ResponseEntity.ok(convertToDTO(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<?> deleteRestaurant(@PathVariable Long id) {
        try {
            restaurantService.deleteRestaurant(id);
            return ResponseEntity.ok(Map.of("message", "Restaurant deleted successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private RestaurantDTO convertToDTO(Restaurant restaurant) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(restaurant.getId());
        dto.setName(restaurant.getName());
        dto.setAddress(restaurant.getAddress());
        dto.setContact(restaurant.getContact());
        dto.setCategory(restaurant.getCategory());
        dto.setWebsite(restaurant.getWebsite());
        if (restaurant.getOwner() != null) {
            dto.setOwnerId(restaurant.getOwner().getId());
            dto.setOwnerName(restaurant.getOwner().getName());
        }
        return dto;
    }
}
