package com.safarnama.backend.data;

import com.safarnama.backend.user.User;
import com.safarnama.backend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final TripRepository tripRepository;

    public SyncController(UserRepository userRepository, CountryRepository countryRepository, TripRepository tripRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.tripRepository = tripRepository;
    }

    @PostMapping("/push")
    public ResponseEntity<?> push(Authentication authentication, @RequestBody SyncPayload payload) {
        User owner = userRepository.findByEmail(authentication.getName());

        // Wipe-and-rebuild, same pattern as before. Order matters here:
        // delete countries (cascades cities/entries/tabs) BEFORE deleting trips,
        // since entries hold a trip_id foreign key — deleting trips first would
        // orphan that FK while the old entries still exist.
        List<CountryData> existingCountries = countryRepository.findByOwner(owner);
        countryRepository.deleteAll(existingCountries);

        List<TripData> existingTrips = tripRepository.findByOwner(owner);
        tripRepository.deleteAll(existingTrips);

        // Build and save trips first, so entries below can look theirs up by name.
        List<TripData> newTrips = new ArrayList<>();
        if (payload.trips != null) {
            for (SyncPayload.TripDTO t : payload.trips) {
                TripData trip = new TripData();
                trip.setOwner(owner);
                trip.setName(t.name);
                trip.setStartDate(t.startDate);
                trip.setEndDate(t.endDate);
                newTrips.add(trip);
            }
        }
        List<TripData> savedTrips = tripRepository.saveAll(newTrips);
        Map<String, TripData> tripsByName = new HashMap<>();
        for (TripData t : savedTrips) {
            tripsByName.put(t.getName(), t);
        }

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
                    entries.add(buildEntry(e, city, null, tripsByName));
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
                        tabEntries.add(buildEntry(e, city, tab, tripsByName));
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

    private PlaceEntryData buildEntry(SyncPayload.EntryDTO e, CityData city, CustomTabData tab, Map<String, TripData> tripsByName) {
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
        entry.setDate(e.date);
        entry.setTrip(e.tripName != null ? tripsByName.get(e.tripName) : null);
        return entry;
    }

    @GetMapping("/pull")
    public ResponseEntity<SyncPullResponse> pull(Authentication authentication) {
        User owner = userRepository.findByEmail(authentication.getName());
        List<CountryData> countries = countryRepository.findByOwner(owner);
        List<TripData> trips = tripRepository.findByOwner(owner);
        return ResponseEntity.ok(new SyncPullResponse(countries, trips));
    }
}