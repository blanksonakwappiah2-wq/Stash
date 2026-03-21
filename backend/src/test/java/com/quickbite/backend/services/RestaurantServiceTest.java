package com.quickbite.backend.services;

import com.quickbite.backend.entities.Restaurant;
import com.quickbite.backend.repositories.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test St");
        testRestaurant.setCategory("Italian");
    }

    @Test
    void testGetAllRestaurants() {
        when(restaurantRepository.findAll()).thenReturn(Arrays.asList(testRestaurant));

        List<Restaurant> restaurants = restaurantService.getAllRestaurants();

        assertEquals(1, restaurants.size());
        verify(restaurantRepository, times(1)).findAll();
    }

    @Test
    void testCreateRestaurant() {
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(invocation -> {
            Restaurant restaurant = invocation.getArgument(0);
            restaurant.setId(1L);
            return restaurant;
        });

        Restaurant result = restaurantService.createRestaurant(testRestaurant);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void testGetRestaurantById_Found() {
        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.of(testRestaurant));

        java.util.Optional<Restaurant> found = restaurantService.getRestaurantById(1L);

        assertTrue(found.isPresent());
        assertEquals("Test Restaurant", found.get().getName());
    }

    @Test
    void testGetRestaurantById_NotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        java.util.Optional<Restaurant> found = restaurantService.getRestaurantById(1L);

        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateRestaurant() {
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        testRestaurant.setName("Updated Name");
        Restaurant result = restaurantService.updateRestaurant(testRestaurant);

        assertEquals("Updated Name", result.getName());
        verify(restaurantRepository).save(testRestaurant);
    }

    @Test
    void testDeleteRestaurant() {
        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.of(testRestaurant));
        doNothing().when(restaurantRepository).delete(testRestaurant);

        restaurantService.deleteRestaurant(1L);

        verify(restaurantRepository).delete(testRestaurant);
    }

    @Test
    void testDeleteRestaurant_NotFound() {
        when(restaurantRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            restaurantService.deleteRestaurant(1L);
        });
    }
}
