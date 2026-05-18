package com.company.scrumpoker.room.dto;

import com.company.scrumpoker.room.model.RoomSettings;
import com.company.scrumpoker.room.model.RoomStatus;
import com.company.scrumpoker.voting.dto.VoteStatsResponse;

import java.util.List;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        String name,
        UUID currentRoundId,
        RoomStatus status,
        int maxVoters,
        RoomSettings settings,
        List<String> deck,
        VoteStatsResponse stats,
        List<ParticipantResponse> participants
) {
}
