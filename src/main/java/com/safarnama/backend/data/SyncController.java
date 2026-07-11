package com.safarnama.backend.data;

import com.safarnama.backend.user.User;
import com.safarnama.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;

    public SyncController(UserRepository userRepository, CountryRepository countryRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
    }

    @PostMapping("/push")
    public ResponseEntity<?> push(Authentication authentication, @RequestBody SyncPayload payload) {
        User owner = userRepository.findByEmail(authentication.getName());

        List<CountryData> existing = countryRepository.findByOwner(owner);
        countryRepository.deleteAll(existing);

        List<CountryData> toSave = new ArrayList<>();
        for (SyncPayload.CountryDTO c : payload.countries) {
            CountryData country = new CountryData();
            country.setOwner(owner);
            country.setName(c.name);
            country.setHome(c.isHome);
            country.setColorHex(c.colorHex);

            List<CityData> cities = new ArrayList<>();
            for (SyncPayload.CityDTO cityDto : c.cities) {
                CityData city = new CityData();
                city.setCountry(country);
                city.setName(cityDto.name);

                List<PlaceEntryData> entries = new ArrayList<>();
                for (SyncPayload.EntryDTO e : cityDto.entries) {
                    entries.add(buildEntry(e, city, null));
                }
                city.setEntries(entries);

                List<CustomTabData> tabs = new ArrayList<>();
                for (SyncPayload.TabDTO t : cityDto.customTabs) {
                    CustomTabData tab = new CustomTabData();
                    tab.setCity(city);
                    tab.setName(t.name);
                    tab.setEmoji(t.emoji);

                    List<PlaceEntryData> tabEntries = new ArrayList<>();
                    for (SyncPayload.EntryDTO e : t.entries) {
                        tabEntries.add(buildEntry(e, city, tab));
                    }
                    tab.setEntries(tabEntries);
                    tabs.add(tab);
                }
                city.setCustomTabs(tabs);
                cities.add(city);
            }
            country.setCities(cities);
            toSave.add(country);
        }

        countryRepository.saveAll(toSave);
        return ResponseEntity.ok("Sync successful");
    }

    private PlaceEntryData buildEntry(SyncPayload.EntryDTO e, CityData city, CustomTabData tab) {
        PlaceEntryData entry = new PlaceEntryData();
        entry.setCity(city);
        entry.setCustomTab(tab);
        entry.setName(e.name);
        entry.setCategory(e.category);
        entry.setNotes(e.notes);
        entry.setRating(e.rating);
        entry.setCuisine(e.cuisine);
        entry.setFoodType(e.foodType);
        entry.setLocationName(e.locationName);
        entry.setLocationAddress(e.locationAddress);
        entry.setLatitude(e.latitude);
        entry.setLongitude(e.longitude);
        entry.setWishlist(e.isWishlist);
        return entry;
    }

    @GetMapping("/pull")
    public ResponseEntity<List<CountryData>> pull(Authentication authentication) {
        User owner = userRepository.findByEmail(authentication.getName());
        List<CountryData> countries = countryRepository.findByOwner(owner);
        return ResponseEntity.ok(countries);
    }
}