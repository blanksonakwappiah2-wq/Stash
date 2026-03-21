package com.quickbite.backend.controllers;

import com.quickbite.backend.dto.OrderDTO;
import com.quickbite.backend.dto.PlaceOrderRequest;
import com.quickbite.backend.entities.Order;
import com.quickbite.backend.entities.OrderItem;
import com.quickbite.backend.entities.OrderStatus;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.entities.UserRole;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.services.OrderService;
import com.quickbite.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER')")
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'RESTAURANT_OWNER', 'DELIVERY_AGENT', 'MANAGER')")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", id));
            return ResponseEntity.ok(convertToDTO(order));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request, Authentication auth) {
        try {
            // Get current user
            User customer = userService.findByEmail(auth.getName());
            if (customer == null) {
                return ResponseEntity.status(401).body(Map.of("message", "User not found"));
            }

            Order order = new Order();
            order.setCustomer(customer);
            order.setDeliveryAddress(request.getDeliveryAddress());

            // Set delivery option
            order.setDeliveryOption(
                    orderService.getDeliveryOptionRepository()
                            .findById(request.getDeliveryOptionId())
                            .orElseThrow(() -> new ResourceNotFoundException("DeliveryOption",
                                    request.getDeliveryOptionId())));

            // Set restaurant
            order.setRestaurant(
                    orderService.getRestaurantRepository()
                            .findById(request.getRestaurantId())
                            .orElseThrow(() -> new ResourceNotFoundException("Restaurant", request.getRestaurantId())));

            // Convert items from request
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                List<OrderItem> orderItems = request.getItems().stream()
                        .map(itemReq -> {
                            OrderItem item = new OrderItem();
                            item.setOrder(order);
                            item.setMenuItem(
                                    orderService.getMenuItemRepository()
                                            .findById(itemReq.getMenuItemId())
                                            .orElseThrow(() -> new ResourceNotFoundException("MenuItem",
                                                    itemReq.getMenuItemId())));
                            item.setQuantity(itemReq.getQuantity());
                            item.setPrice(item.getMenuItem().getPrice());
                            return item;
                        })
                        .collect(Collectors.toList());
                order.setItems(orderItems);
            }

            Order placed = orderService.placeOrder(order);
            return ResponseEntity.ok(convertToDTO(placed));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'DELIVERY_AGENT', 'MANAGER')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        try {
            Order updated = orderService.updateOrderStatus(id, status);
            if (updated == null) {
                throw new ResourceNotFoundException("Order", id);
            }
            return ResponseEntity.ok(convertToDTO(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'MANAGER')")
    public ResponseEntity<?> assignAgent(@PathVariable Long id, @RequestParam Long agentId) {
        try {
            User agent = userService.getUserById(agentId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", agentId));

            // Verify agent has DELIVERY_AGENT role
            if (agent.getRole() != UserRole.DELIVERY_AGENT) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "User is not a delivery agent"));
            }

            Order assigned = orderService.assignDeliveryAgent(id, agent);
            if (assigned == null) {
                throw new ResourceNotFoundException("Order", id);
            }
            return ResponseEntity.ok(convertToDTO(assigned));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER')")
    public List<OrderDTO> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER', 'MANAGER')")
    public List<OrderDTO> getOrdersByRestaurant(@PathVariable Long restaurantId) {
        return orderService.getOrdersByRestaurant(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/agent/{agentId}")
    @PreAuthorize("hasAnyRole('DELIVERY_AGENT', 'MANAGER')")
    public List<OrderDTO> getOrdersByAgent(@PathVariable Long agentId) {
        return orderService.getOrdersByDeliveryAgent(agentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        dto.setCustomerName(order.getCustomer() != null ? order.getCustomer().getName() : null);
        dto.setRestaurantId(order.getRestaurant() != null ? order.getRestaurant().getId() : null);
        dto.setRestaurantName(order.getRestaurant() != null ? order.getRestaurant().getName() : null);
        dto.setStatus(order.getStatus().toString());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDeliveryFee(order.getDeliveryFee());
        dto.setDistance(order.getDistance());
        dto.setDeliveryAddress(order.getDeliveryAddress());
        dto.setDeliveryOptionId(order.getDeliveryOption() != null ? order.getDeliveryOption().getId() : null);
        dto.setOrderTime(order.getOrderTime());
        dto.setDeliveryTime(order.getDeliveryTime());
        dto.setDeliveryAgentId(order.getDeliveryAgent() != null ? order.getDeliveryAgent().getId() : null);
        dto.setDeliveryAgentName(order.getDeliveryAgent() != null ? order.getDeliveryAgent().getName() : null);
        return dto;
    }
}
