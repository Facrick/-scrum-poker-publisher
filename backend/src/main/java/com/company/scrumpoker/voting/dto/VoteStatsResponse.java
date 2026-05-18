package com.company.scrumpoker.voting.dto;

public record VoteStatsResponse(
        int votedCount,
        int totalVoters,
        Double average,
        String min,
        String max,
        boolean consensus
) {
}
