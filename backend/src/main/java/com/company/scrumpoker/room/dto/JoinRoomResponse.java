package com.company.scrumpoker.room.dto;

import java.util.UUID;

public record JoinRoomResponse(
        UUID roomId,
        UUID participantId,
        String participantName
) {
}
