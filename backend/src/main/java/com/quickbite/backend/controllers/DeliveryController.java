package com.quickbite.backend.controllers;

import com.quickbite.backend.entities.Order;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.repositories.OrderRepository;
import com.quickbite.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "*")
public class DeliveryController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/agents")
    public List<User> getAgents() {
        return userRepository.findByRole(User.UserRole.DELIVERY_AGENT);
    }

    @PostMapping("/agents")
    public ResponseEntity<?> addAgent(@RequestBody User agent) {
        agent.setRole(User.UserRole.DELIVERY_AGENT);
        if (userRepository.findByEmail(agent.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Agent with this email already exists.");
        }
        return ResponseEntity.ok(userRepository.save(agent));
    }

    @PutMapping("/location")
    public ResponseEntity<?> updateLocation(@RequestBody Map<String, Object> locationData) {
        Long agentId = ((Number) locationData.get("agentId")).longValue();
        Double lat = ((Number) locationData.get("latitude")).doubleValue();
        Double lng = ((Number) locationData.get("longitude")).doubleValue();

        User agent = userRepository.findById(agentId).orElse(null);
        if (agent == null || agent.getRole() != User.UserRole.DELIVERY_AGENT) {
            return ResponseEntity.badRequest().body("Invalid delivery agent.");
        }

        agent.setLatitude(lat);
        agent.setLongitude(lng);
        userRepository.save(agent);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/track/{orderId}")
    public ResponseEntity<?> trackOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        User agent = order.getDeliveryAgent();
        if (agent == null) {
            return ResponseEntity.ok(Map.of(
                "status", order.getStatus(),
                "message", "Agent not yet assigned"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "orderId", order.getId(),
            "status", order.getStatus(),
            "agentName", agent.getName(),
            "latitude", agent.getLatitude() != null ? agent.getLatitude() : 0.0,
            "longitude", agent.getLongitude() != null ? agent.getLongitude() : 0.0,
            "destination", order.getDeliveryAddress()
        ));
    }
}
