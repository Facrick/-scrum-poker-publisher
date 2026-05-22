package com.company.scrumpoker.room.dto;

import com.company.scrumpoker.room.model.DeckType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateRoomSettingsRequest(
        // moderatorId больше не нужен
        @NotNull
        DeckType deckType,

        @Size(max = 50)
        List<@Size(max = 20) String> customDeck,

        boolean autoReveal,
        boolean observersAllowed,
        boolean timerEnabled,

        @Min(30)
        @Max(7200)
        int timerDurationSeconds
) {
}
