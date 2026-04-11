package com.example.agentapp.excursion.model;

public enum RoomType {

    SINGLE("Single Room"),       // Tek kişilik
    DOUBLE("Double Room"),       // Çift kişilik (1 büyük yatak)
    TWIN("Twin Room"),           // İki tek kişilik yatak
    TRIPLE("Triple Room"),       // Üç kişilik
    SUITE("Suite"),              // Suit oda
    FAMILY("Family Room");       // Aile odası

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}