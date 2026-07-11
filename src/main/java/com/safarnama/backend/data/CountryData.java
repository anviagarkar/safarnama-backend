package com.safarnama.backend.data;

import com.safarnama.backend.user.User;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "countries")
public class CountryData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User owner;

    private String name;
    private boolean isHome;
    private String colorHex;

    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CityData> cities;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isHome() { return isHome; }
    public void setHome(boolean home) { isHome = home; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public List<CityData> getCities() { return cities; }
    public void setCities(List<CityData> cities) { this.cities = cities; }
}