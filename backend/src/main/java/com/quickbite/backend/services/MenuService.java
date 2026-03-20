package com.quickbite.backend.services;

import com.quickbite.backend.dto.MenuItemDTO;
import com.quickbite.backend.dto.MenuItemRequest;
import com.quickbite.backend.entities.MenuItem;
import com.quickbite.backend.entities.Restaurant;
import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.repositories.MenuItemRepository;
import com.quickbite.backend.repositories.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<MenuItemDTO> getMenuByRestaurantId(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", restaurantId));

        List<MenuItem> menuItems = menuItemRepository.findByRestaurantId(restaurantId);
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MenuItemDTO> getAllMenuItems() {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        return menuItems.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MenuItemDTO getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));
        return convertToDTO(menuItem);
    }

    public MenuItemDTO createMenuItem(MenuItemRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Menu item name is required");
        }
        if (request.getPrice() <= 0) {
            throw new BadRequestException("Price must be greater than 0");
        }

        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", request.getRestaurantId()));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setRestaurant(restaurant);

        menuItem = menuItemRepository.save(menuItem);
        return convertToDTO(menuItem);
    }

    public MenuItemDTO updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem existingItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", id));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            existingItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existingItem.setDescription(request.getDescription());
        }
        if (request.getPrice() > 0) {
            existingItem.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            existingItem.setImageUrl(request.getImageUrl());
        }
        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant", request.getRestaurantId()));
            existingItem.setRestaurant(restaurant);
        }

        existingItem = menuItemRepository.save(existingItem);
        return convertToDTO(existingItem);
    }

    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("MenuItem", id);
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItemDTO convertToDTO(MenuItem menuItem) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDescription(menuItem.getDescription());
        dto.setPrice(menuItem.getPrice());
        dto.setImageUrl(menuItem.getImageUrl());
        dto.setRestaurantId(menuItem.getRestaurant().getId());
        dto.setRestaurantName(menuItem.getRestaurant().getName());
        return dto;
    }
}
