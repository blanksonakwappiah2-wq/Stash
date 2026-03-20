package com.quickbite.backend.dto;

import java.util.List;

public class PlaceOrderRequest {
    private Long customerId;
    private Long restaurantId;
    private String deliveryAddress;
    private Double customerLatitude;
    private Double customerLongitude;
    private Long deliveryOptionId;
    private List<OrderItemRequest> items;

    public PlaceOrderRequest() {}

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getRestaurantId() { return restaurantId; }
    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public Double getCustomerLatitude() { return customerLatitude; }
    public void setCustomerLatitude(Double customerLatitude) { this.customerLatitude = customerLatitude; }
    public Double getCustomerLongitude() { return customerLongitude; }
    public void setCustomerLongitude(Double customerLongitude) { this.customerLongitude = customerLongitude; }
    public Long getDeliveryOptionId() { return deliveryOptionId; }
    public void setDeliveryOptionId(Long deliveryOptionId) { this.deliveryOptionId = deliveryOptionId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }

    public static class OrderItemRequest {
        private Long menuItemId;
        private int quantity;

        public OrderItemRequest() {}

        public Long getMenuItemId() { return menuItemId; }
        public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
