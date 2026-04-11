package com.example.agentapp.excursion.model;

public enum TransferType {

    AIRPORT_PICKUP("Airport Pickup"),      // Havaalanı karşılama
    AIRPORT_DROPOFF("Airport Drop-off"),   // Havaalanı gidiş
    HOTEL_TO_HOTEL("Hotel to Hotel"),      // Otel arası
    EXCURSION("Excursion Transfer"),       // Gezi transferi
    CITY_TOUR("City Tour Transfer"),       // Şehir turu transferi
    INTER_CITY("Inter-city Transfer");     // Şehirlerarası

    private final String displayName;

    TransferType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}