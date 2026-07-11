package com.safarnama.backend.data;

import java.util.List;

public class SyncPayload {
    public List<CountryDTO> countries;

    public static class CountryDTO {
        public String name;
        public boolean isHome;
        public String colorHex;
        public List<CityDTO> cities;
    }

    public static class CityDTO {
        public String name;
        public List<EntryDTO> entries;
        public List<TabDTO> customTabs;
    }

    public static class TabDTO {
        public String name;
        public String emoji;
        public List<EntryDTO> entries;
    }

    public static class EntryDTO {
        public String name;
        public String category;
        public String notes;
        public int rating;
        public String cuisine;
        public String foodType;
        public String locationName;
        public String locationAddress;
        public Double latitude;
        public Double longitude;
        public boolean isWishlist;
    }
}