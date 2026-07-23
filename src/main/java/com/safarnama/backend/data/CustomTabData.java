package com.safarnama.backend.data;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "custom_tabs")
public class CustomTabData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private CityData city;

    private String name;
    private String emoji;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CityData getCity() { return city; }
    public void setCity(CityData city) { this.city = city; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    @OneToMany(mappedBy = "customTab", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceEntryData> entries;

    public List<PlaceEntryData> getEntries() { return entries; }
    public void setEntries(List<PlaceEntryData> entries) { this.entries = entries; }
}