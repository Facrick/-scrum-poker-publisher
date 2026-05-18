package com.company.scrumpoker.voting.service;

import com.company.scrumpoker.common.exception.BadRequestException;
import com.company.scrumpoker.common.exception.NotFoundException;
import com.company.scrumpoker.participant.entity.ParticipantEntity;
import com.company.scrumpoker.participant.repository.ParticipantRepository;
import com.company.scrumpoker.room.dto.RoomResponse;
import com.company.scrumpoker.room.entity.RoomEntity;
import com.company.scrumpoker.room.model.ParticipantRole;
import com.company.scrumpoker.room.model.RoomSettings;
import com.company.scrumpoker.room.model.RoomStatus;
import com.company.scrumpoker.room.service.RoomService;
import com.company.scrumpoker.voting.dto.CastVoteRequest;
import com.company.scrumpoker.voting.entity.VoteEntity;
import com.company.scrumpoker.voting.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VotingService {

    private final RoomService roomService;
    private final ParticipantRepository participantRepository;
    private final VoteRepository voteRepository;

    @Transactional
    public RoomResponse castVote(UUID roomId, CastVoteRequest request) {
        RoomEntity room = roomService.getRoomEntity(roomId);
        RoomSettings settings = roomService.getSettings(room);
        List<String> deck = roomService.resolveDeck(settings);

        if (room.getStatus() == RoomStatus.REVEALED) {
            throw new BadRequestException("Voting is already revealed. Reset votes to start a new round.");
        }

        if (!deck.contains(request.value())) {
            throw new BadRequestException("Unsupported vote value for current deck: " + request.value());
        }

        ParticipantEntity participant = participantRepository.findById(request.participantId())
                .orElseThrow(() -> new NotFoundException("Participant not found: " + request.participantId()));

        if (!participant.getRoomId().equals(roomId)) {
            throw new BadRequestException("Participant does not belong to this room.");
        }

        if (participant.getRole() == ParticipantRole.OBSERVER) {
            throw new BadRequestException("Observers cannot vote.");
        }

        VoteEntity vote = voteRepository.findByRoundIdAndParticipantId(
                        room.getCurrentRoundId(),
                        participant.getId()
                )
                .orElseGet(() -> VoteEntity.builder()
                        .id(UUID.randomUUID())
                        .roomId(roomId)
                        .roundId(room.getCurrentRoundId())
                        .participantId(participant.getId())
                        .build()
                );

        vote.setValue(request.value());
        vote.setVotedAt(Instant.now());

        voteRepository.save(vote);

        RoomResponse response = roomService.buildRoomResponse(room);

        if (settings.autoReveal() && response.stats().votedCount() >= response.stats().totalVoters()) {
            room.setStatus(RoomStatus.REVEALED);
            return roomService.buildRoomResponse(room);
        }

        return response;
    }
}
