package com.company.scrumpoker.room.dto;

import com.company.scrumpoker.room.model.ParticipantRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JoinRoomRequest(
        @NotBlank
        @Size(max = 80)
        String name,

        @NotNull
        ParticipantRole role
) {
}
