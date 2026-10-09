package com.safarnama.backend.data;

import java.util.List;

// Wraps both countries and trips together so /sync/pull can return both
// in one response instead of a bare array like before.
public class SyncPullResponse {
    public List<CountryData> countries;
    public List<TripData> trips;

    public SyncPullResponse(List<CountryData> countries, List<TripData> trips) {
        this.countries = countries;
        this.trips = trips;
    }
}