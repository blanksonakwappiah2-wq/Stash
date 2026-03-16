package com.quickbite.backend.controllers;

import com.quickbite.backend.entities.Order;
import com.quickbite.backend.entities.OrderStatus;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.services.OrderService;
import com.quickbite.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id).orElse(null);
    }

    @PostMapping
    public Order placeOrder(@RequestBody Order order) {
        return orderService.placeOrder(order);
    }

    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(id, status);
    }

    @PutMapping("/{id}/assign")
    public Order assignAgent(@PathVariable Long id, @RequestParam Long agentId) {
        User agent = userService.getUserById(agentId).orElse(null);
        if (agent != null) {
            return orderService.assignDeliveryAgent(id, agent);
        }
        return null;
    }

    @GetMapping("/customer/{customerId}")
    public List<Order> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @GetMapping("/restaurant/{restaurantId}")
    public List<Order> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        return orderService.getOrdersByRestaurant(restaurantId);
    }

    @GetMapping("/agent/{agentId}")
    public List<Order> getOrdersByAgent(@PathVariable Long agentId) {
        return orderService.getOrdersByDeliveryAgent(agentId);
    }
}