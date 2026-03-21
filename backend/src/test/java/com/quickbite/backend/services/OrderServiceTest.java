package com.quickbite.backend.services;

import com.quickbite.backend.entities.*;
import com.quickbite.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private DistanceService distanceService;

    @Mock
    private DeliveryOptionRepository deliveryOptionRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private Restaurant testRestaurant;
    private DeliveryOption testDeliveryOption;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test St");

        testDeliveryOption = new DeliveryOption();
        testDeliveryOption.setId(1L);
        testDeliveryOption.setMethod("standard");
        testDeliveryOption.setBaseFee(2.99);
        testDeliveryOption.setPerKmFee(0.50);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setRestaurant(testRestaurant);
        testOrder.setDeliveryOption(testDeliveryOption);
        testOrder.setDeliveryAddress("456 Delivery Ave");
    }

    @Test
    void testGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));

        List<Order> orders = orderService.getAllOrders();

        assertEquals(1, orders.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetOrderById_Found() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Optional<Order> found = orderService.getOrderById(1L);

        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getId());
    }

    @Test
    void testPlaceOrder() {
        when(distanceService.getDistance("123 Test St", "456 Delivery Ave")).thenReturn(5.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        Order result = orderService.placeOrder(testOrder);

        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(5.0, result.getDistance());
        assertEquals(2.99 + (5.0 * 0.50), result.getDeliveryFee());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void testUpdateOrderStatus_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Order result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        assertNull(result);
    }

    @Test
    void testAssignDeliveryAgent() {
        User agent = new User();
        agent.setId(2L);
        agent.setRole(UserRole.DELIVERY_AGENT);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.assignDeliveryAgent(1L, agent);

        assertNotNull(result);
        assertEquals(agent, result.getDeliveryAgent());
        assertEquals(OrderStatus.DELIVERING, result.getStatus());
    }

    @Test
    void testGetOrdersByCustomer() {
        when(orderRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(testOrder));

        List<Order> orders = orderService.getOrdersByCustomer(1L);

        assertEquals(1, orders.size());
        verify(orderRepository).findByCustomerId(1L);
    }

    @Test
    void testGetOrdersByRestaurant() {
        when(orderRepository.findByRestaurantId(1L)).thenReturn(Arrays.asList(testOrder));

        List<Order> orders = orderService.getOrdersByRestaurant(1L);

        assertEquals(1, orders.size());
    }

    @Test
    void testGetOrdersByDeliveryAgent() {
        when(orderRepository.findByDeliveryAgentId(1L)).thenReturn(Arrays.asList(testOrder));

        List<Order> orders = orderService.getOrdersByDeliveryAgent(1L);

        assertEquals(1, orders.size());
    }
}
