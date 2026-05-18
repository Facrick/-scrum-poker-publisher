package com.company.scrumpoker.room.model;

import java.util.List;

public record RoomSettings(
        DeckType deckType,
        List<String> customDeck,
        boolean autoReveal,
        boolean observersAllowed,
        boolean timerEnabled,
        int timerDurationSeconds
) {
    public static RoomSettings defaults() {
        return new RoomSettings(
                DeckType.FIBONACCI,
                List.of(),
                false,
                true,
                false,
                300
        );
    }
}
