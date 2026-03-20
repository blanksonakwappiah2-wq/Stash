package com.quickbite.backend.controllers;

import com.quickbite.backend.dto.MenuItemDTO;
import com.quickbite.backend.dto.MenuItemRequest;
import com.quickbite.backend.services.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public List<MenuItemDTO> getAllMenuItems() {
        return menuService.getAllMenuItems();
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<MenuItemDTO> getMenuByRestaurantId(@PathVariable Long restaurantId) {
        return menuService.getMenuByRestaurantId(restaurantId);
    }

    @GetMapping("/{id}")
    public MenuItemDTO getMenuItemById(@PathVariable Long id) {
        return menuService.getMenuItemById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'RESTAURANT_OWNER')")
    public ResponseEntity<?> createMenuItem(@RequestBody MenuItemRequest request) {
        try {
            MenuItemDTO created = menuService.createMenuItem(request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESTAURANT_OWNER')")
    public ResponseEntity<?> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        try {
            MenuItemDTO updated = menuService.updateMenuItem(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RESTAURANT_OWNER')")
    public ResponseEntity<?> deleteMenuItem(@PathVariable Long id) {
        try {
            menuService.deleteMenuItem(id);
            return ResponseEntity.ok(Map.of("message", "Menu item deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
