package com.safarnama.backend.data;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "cities")
public class CityData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CountryData country;

    private String name;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceEntryData> entries;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomTabData> customTabs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CountryData getCountry() { return country; }
    public void setCountry(CountryData country) { this.country = country; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<PlaceEntryData> getEntries() { return entries; }
    public void setEntries(List<PlaceEntryData> entries) { this.entries = entries; }
    public List<CustomTabData> getCustomTabs() { return customTabs; }
    public void setCustomTabs(List<CustomTabData> customTabs) { this.customTabs = customTabs; }
}