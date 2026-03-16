package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
}