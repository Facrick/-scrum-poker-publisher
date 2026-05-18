package com.company.scrumpoker.room.mapper;

import com.company.scrumpoker.participant.entity.ParticipantEntity;
import com.company.scrumpoker.room.dto.ParticipantResponse;
import com.company.scrumpoker.room.dto.RoomResponse;
import com.company.scrumpoker.room.entity.RoomEntity;
import com.company.scrumpoker.room.model.ParticipantRole;
import com.company.scrumpoker.room.model.RoomSettings;
import com.company.scrumpoker.voting.dto.VoteStatsResponse;
import com.company.scrumpoker.voting.entity.VoteEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RoomMapper {

    public RoomResponse toResponse(
            RoomEntity room,
            RoomSettings settings,
            List<String> deck,
            List<ParticipantEntity> participants,
            List<VoteEntity> votes,
            boolean revealVotes,
            int maxVoters
    ) {
        Map<UUID, VoteEntity> votesByParticipantId = votes.stream()
                .collect(Collectors.toMap(VoteEntity::getParticipantId, Function.identity()));

        List<ParticipantResponse> participantResponses = participants.stream()
                .map(participant -> {
                    VoteEntity vote = votesByParticipantId.get(participant.getId());

                    return new ParticipantResponse(
                            participant.getId(),
                            participant.getName(),
                            participant.getRole(),
                            participant.isConnected(),
                            vote != null,
                            revealVotes && vote != null ? vote.getValue() : null
                    );
                })
                .toList();

        VoteStatsResponse stats = buildStats(participants, votes, revealVotes);

        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getCurrentRoundId(),
                room.getStatus(),
                maxVoters,
                settings,
                deck,
                stats,
                participantResponses
        );
    }

    private VoteStatsResponse buildStats(
            List<ParticipantEntity> participants,
            List<VoteEntity> votes,
            boolean revealVotes
    ) {
        int totalVoters = (int) participants.stream()
                .filter(participant -> participant.getRole() != ParticipantRole.OBSERVER)
                .count();

        int votedCount = votes.size();

        if (!revealVotes || votes.isEmpty()) {
            return new VoteStatsResponse(votedCount, totalVoters, null, null, null, false);
        }

        List<Integer> numericVotes = votes.stream()
                .map(VoteEntity::getValue)
                .filter(value -> value.matches("-?\\d+"))
                .map(Integer::parseInt)
                .toList();

        Double average = numericVotes.isEmpty()
                ? null
                : numericVotes.stream().mapToInt(Integer::intValue).average().orElse(0);

        String min = numericVotes.stream()
                .min(Comparator.naturalOrder())
                .map(String::valueOf)
                .orElse(null);

        String max = numericVotes.stream()
                .max(Comparator.naturalOrder())
                .map(String::valueOf)
                .orElse(null);

        boolean consensus = votes.stream()
                .map(VoteEntity::getValue)
                .distinct()
                .count() == 1;

        return new VoteStatsResponse(votedCount, totalVoters, average, min, max, consensus);
    }
}
