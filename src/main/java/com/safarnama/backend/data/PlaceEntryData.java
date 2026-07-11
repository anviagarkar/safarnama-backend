package com.safarnama.backend.data;

import jakarta.persistence.*;

@Entity
@Table(name = "place_entries")
public class PlaceEntryData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CityData city;

    @ManyToOne
    private CustomTabData customTab;

    private String name;
    private String category;
    private String notes;
    private int rating;
    private String cuisine;
    private String foodType;
    private String locationName;
    private String locationAddress;
    private Double latitude;
    private Double longitude;
    private boolean isWishlist;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CityData getCity() { return city; }
    public void setCity(CityData city) { this.city = city; }
    public CustomTabData getCustomTab() { return customTab; }
    public void setCustomTab(CustomTabData customTab) { this.customTab = customTab; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isWishlist() { return isWishlist; }
    public void setWishlist(boolean wishlist) { isWishlist = wishlist; }
}