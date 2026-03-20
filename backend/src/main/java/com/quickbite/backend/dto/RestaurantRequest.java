package com.quickbite.backend.dto;

public class RestaurantRequest {
    private String name;
    private String address;
    private String contact;
    private String category;
    private String website;
    private Long ownerId;

    public RestaurantRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
