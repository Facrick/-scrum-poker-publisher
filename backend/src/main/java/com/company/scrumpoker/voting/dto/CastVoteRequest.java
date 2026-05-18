package com.company.scrumpoker.voting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CastVoteRequest(
        @NotNull
        UUID participantId,

        @NotBlank
        String value
) {
}
