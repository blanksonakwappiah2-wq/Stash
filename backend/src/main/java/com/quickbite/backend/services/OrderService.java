package com.quickbite.backend.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.quickbite.backend.entities.Order;
import com.quickbite.backend.entities.OrderItem;
import com.quickbite.backend.entities.OrderStatus;
import com.quickbite.backend.entities.User;
import com.quickbite.backend.repositories.DeliveryOptionRepository;
import com.quickbite.backend.repositories.MenuItemRepository;
import com.quickbite.backend.repositories.OrderItemRepository;
import com.quickbite.backend.repositories.OrderRepository;
import com.quickbite.backend.repositories.RestaurantRepository;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private DistanceService distanceService;

    // Expose repositories for controller use
    @Autowired
    private DeliveryOptionRepository deliveryOptionRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private MenuItemRepository menuItemRepository;

    public DeliveryOptionRepository getDeliveryOptionRepository() {
        return deliveryOptionRepository;
    }

    public RestaurantRepository getRestaurantRepository() {
        return restaurantRepository;
    }

    public MenuItemRepository getMenuItemRepository() {
        return menuItemRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order placeOrder(Order order) {
        // Calculate total from items
        double total = 0;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                total += item.getMenuItem().getPrice() * item.getQuantity();
            }
        }
        order.setTotalAmount(total);

        // Calculate distance and delivery fee
        String origin = order.getRestaurant().getAddress();
        String destination = order.getDeliveryAddress();
        double distance = distanceService.getDistance(origin, destination);
        order.setDistance(distance);
        double baseFee = order.getDeliveryOption().getBaseFee();
        double perKmFee = order.getDeliveryOption().getPerKmFee();
        double deliveryFee = baseFee + (distance * perKmFee);
        order.setDeliveryFee(deliveryFee);

        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Optional<Order> optOrder = orderRepository.findById(orderId);
        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            order.setStatus(status);
            return orderRepository.save(order);
        }
        return null;
    }

    public Order assignDeliveryAgent(Long orderId, User agent) {
        Optional<Order> optOrder = orderRepository.findById(orderId);
        if (optOrder.isPresent()) {
            Order order = optOrder.get();
            order.setDeliveryAgent(agent);
            order.setStatus(OrderStatus.DELIVERING);
            return orderRepository.save(order);
        }
        return null;
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> getOrdersByDeliveryAgent(Long agentId) {
        return orderRepository.findByDeliveryAgentId(agentId);
    }
}