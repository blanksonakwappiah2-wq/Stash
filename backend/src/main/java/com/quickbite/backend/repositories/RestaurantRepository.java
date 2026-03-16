package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}