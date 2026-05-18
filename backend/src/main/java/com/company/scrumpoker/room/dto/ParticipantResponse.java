package com.company.scrumpoker.room.dto;

import com.company.scrumpoker.room.model.ParticipantRole;

import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        String name,
        ParticipantRole role,
        boolean connected,
        boolean voted,
        String vote
) {
}
