package com.safarnama.backend.data;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity
@Table(name = "place_entries")
public class PlaceEntryData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private CityData city;

    @ManyToOne
    @JsonIgnore
    private CustomTabData customTab;

    @ManyToOne
    @JsonIgnore
    private TripData trip;

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

    // The live database column is actually named "is_wishlist" (visible in
    // Hibernate's own error hint when this mapping was missing: "column
    // e1_0.wishlist does not exist ... perhaps you meant is_wishlist"). Without
    // this annotation, Hibernate's default naming strategy maps the "wishlist"
    // field straight to a column called "wishlist", which doesn't exist — any
    // query touching a user's place entries (push's cascade-delete, pull's
    // serialization) would throw and the request would fail before a normal
    // response could be built.
    @Column(name = "is_wishlist")
    private boolean wishlist;

    private Instant date;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CityData getCity() { return city; }
    public void setCity(CityData city) { this.city = city; }

    public CustomTabData getCustomTab() { return customTab; }
    public void setCustomTab(CustomTabData customTab) { this.customTab = customTab; }

    public TripData getTrip() { return trip; }
    public void setTrip(TripData trip) { this.trip = trip; }

    // Synthetic read-only property — NOT @JsonIgnore'd, so it serializes as
    // a plain "tripName" string in the JSON (matching iOS's EntryResponse.tripName)
    // without exposing the full TripData object the way getTrip() would.
    public String getTripName() {
        return trip != null ? trip.getName() : null;
    }

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

    public boolean isWishlist() { return wishlist; }
    public void setWishlist(boolean wishlist) { this.wishlist = wishlist; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }
}