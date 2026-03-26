package com.quickbite.backend.dto;

public class RestaurantRequest {
    private String name;
    private String address;
    private String contact;
    private String category;
    private String website;
    private Long ownerId;

    public RestaurantRequest() {}

    private String imageUrl;
    
    // New fields for Inline Owner Creation
    private String ownerName;
    private String ownerEmail;
    private String ownerPassword;

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
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    
    public String getOwnerPassword() { return ownerPassword; }
    public void setOwnerPassword(String ownerPassword) { this.ownerPassword = ownerPassword; }
}
