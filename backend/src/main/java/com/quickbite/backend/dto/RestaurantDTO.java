package com.quickbite.backend.dto;

public class RestaurantDTO {
    private Long id;
    private String name;
    private String address;
    private String contact;
    private String category;
    private String website;
    private Long ownerId;
    private String ownerName;

    public RestaurantDTO() {}

    private String imageUrl;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
