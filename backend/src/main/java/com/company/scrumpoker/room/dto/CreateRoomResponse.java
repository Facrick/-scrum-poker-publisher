package com.company.scrumpoker.room.dto;

import java.util.UUID;

public record CreateRoomResponse(
        UUID roomId,
        UUID participantId,
        String roomName
) {
}
