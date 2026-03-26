package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.Order;
import com.quickbite.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByRestaurantId(Long restaurantId);
    List<Order> findByDeliveryAgentId(Long agentId);
    List<Order> findByDeliveryAgent(User agent);
}