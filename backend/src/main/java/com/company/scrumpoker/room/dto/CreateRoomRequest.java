package com.company.scrumpoker.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
        @NotBlank
        @Size(max = 120)
        String roomName
) {
}
