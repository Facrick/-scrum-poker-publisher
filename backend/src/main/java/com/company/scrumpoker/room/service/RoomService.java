package com.company.scrumpoker.room.service;

import com.company.scrumpoker.common.exception.BadRequestException;
import com.company.scrumpoker.common.exception.NotFoundException;
import com.company.scrumpoker.common.util.JsonUtils;
import com.company.scrumpoker.participant.entity.ParticipantEntity;
import com.company.scrumpoker.participant.repository.ParticipantRepository;
import com.company.scrumpoker.room.dto.*;
import com.company.scrumpoker.room.entity.RoomEntity;
import com.company.scrumpoker.room.mapper.RoomMapper;
import com.company.scrumpoker.room.model.DeckType;
import com.company.scrumpoker.room.model.ParticipantRole;
import com.company.scrumpoker.room.model.RoomSettings;
import com.company.scrumpoker.room.model.RoomStatus;
import com.company.scrumpoker.room.repository.RoomRepository;
import com.company.scrumpoker.voting.entity.VoteEntity;
import com.company.scrumpoker.voting.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final List<String> FIBONACCI_DECK = List.of("0", "1", "2", "3", "5", "8", "13", "21", "?", "☕");
    private static final List<String> T_SHIRT_DECK = List.of("XS", "S", "M", "L", "XL", "XXL", "?", "☕");

    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final VoteRepository voteRepository;
    private final RoomMapper roomMapper;
    private final JsonUtils jsonUtils;

    @Value("${app.room.max-voters:100}")
    private int maxVoters;

    @Transactional
    public CreateRoomResponse create(CreateRoomRequest request) {
        UUID roomId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        RoomSettings settings = RoomSettings.defaults();

        RoomEntity room = RoomEntity.builder()
                .id(roomId)
                .name(request.roomName())
                .currentRoundId(roundId)
                .status(RoomStatus.VOTING)
                .createdAt(Instant.now())
                .settingsJson(jsonUtils.toJson(settings))
                .build();

        ParticipantEntity moderator = ParticipantEntity.builder()
                .id(moderatorId)
                .roomId(roomId)
                .name(request.moderatorName())
                .role(ParticipantRole.MODERATOR)
                .connected(true)
                .joinedAt(Instant.now())
                .build();

        roomRepository.save(room);
        participantRepository.save(moderator);

        return new CreateRoomResponse(roomId, moderatorId, room.getName());
    }

    @Transactional
    public JoinRoomResponse join(UUID roomId, JoinRoomRequest request) {
        RoomEntity room = getRoomEntity(roomId);
        RoomSettings settings = getSettings(room);

        if (request.role() == ParticipantRole.OBSERVER && !settings.observersAllowed()) {
            throw new BadRequestException("Observers are disabled in this room.");
        }

        if (request.role() == ParticipantRole.PARTICIPANT || request.role() == ParticipantRole.MODERATOR) {
            long voters = participantRepository.countByRoomIdAndRoleIn(
                    roomId,
                    List.of(ParticipantRole.PARTICIPANT, ParticipantRole.MODERATOR)
            );

            if (voters >= maxVoters) {
                throw new BadRequestException("Room voting limit exceeded. Maximum voters: " + maxVoters);
            }
        }

        ParticipantEntity participant = ParticipantEntity.builder()
                .id(UUID.randomUUID())
                .roomId(roomId)
                .name(request.name())
                .role(request.role())
                .connected(true)
                .joinedAt(Instant.now())
                .build();

        participantRepository.save(participant);

        return new JoinRoomResponse(roomId, participant.getId(), participant.getName());
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(UUID roomId) {
        RoomEntity room = getRoomEntity(roomId);
        return buildRoomResponse(room);
    }

    @Transactional
    public RoomResponse reveal(UUID roomId, UUID moderatorId) {
        RoomEntity room = getRoomEntity(roomId);
        requireModerator(roomId, moderatorId);

        room.setStatus(RoomStatus.REVEALED);
        roomRepository.save(room);

        return buildRoomResponse(room);
    }

    @Transactional
    public RoomResponse reset(UUID roomId, UUID moderatorId) {
        RoomEntity room = getRoomEntity(roomId);
        requireModerator(roomId, moderatorId);

        voteRepository.deleteByRoomIdAndRoundId(roomId, room.getCurrentRoundId());

        room.setCurrentRoundId(UUID.randomUUID());
        room.setStatus(RoomStatus.VOTING);

        roomRepository.save(room);

        return buildRoomResponse(room);
    }

    @Transactional
    public RoomResponse updateSettings(UUID roomId, UpdateRoomSettingsRequest request) {
        RoomEntity room = getRoomEntity(roomId);
        requireModerator(roomId, request.moderatorId());

        RoomSettings settings = new RoomSettings(
                request.deckType(),
                request.customDeck() == null ? List.of() : request.customDeck(),
                request.autoReveal(),
                request.observersAllowed(),
                request.timerEnabled(),
                request.timerDurationSeconds()
        );

        validateSettings(settings);

        room.setSettingsJson(jsonUtils.toJson(settings));
        roomRepository.save(room);

        return buildRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public RoomResponse buildRoomResponse(RoomEntity room) {
        RoomSettings settings = getSettings(room);
        List<String> deck = resolveDeck(settings);

        List<ParticipantEntity> participants = participantRepository.findByRoomIdOrderByJoinedAtAsc(room.getId());
        List<VoteEntity> votes = voteRepository.findByRoomIdAndRoundId(room.getId(), room.getCurrentRoundId());

        return roomMapper.toResponse(
                room,
                settings,
                deck,
                participants,
                votes,
                room.getStatus() == RoomStatus.REVEALED,
                maxVoters
        );
    }

    @Transactional(readOnly = true)
    public RoomEntity getRoomEntity(UUID roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found: " + roomId));
    }

    public RoomSettings getSettings(RoomEntity room) {
        if (room.getSettingsJson() == null || room.getSettingsJson().isBlank()) {
            return RoomSettings.defaults();
        }

        return jsonUtils.fromJson(room.getSettingsJson(), RoomSettings.class);
    }

    public List<String> resolveDeck(RoomSettings settings) {
        if (settings.deckType() == DeckType.T_SHIRT) {
            return T_SHIRT_DECK;
        }

        if (settings.deckType() == DeckType.CUSTOM) {
            return settings.customDeck();
        }

        return FIBONACCI_DECK;
    }

    public void requireModerator(UUID roomId, UUID participantId) {
        ParticipantEntity participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Participant not found: " + participantId));

        if (!participant.getRoomId().equals(roomId)) {
            throw new BadRequestException("Participant does not belong to this room.");
        }

        if (participant.getRole() != ParticipantRole.MODERATOR) {
            throw new BadRequestException("Only moderator can perform this action.");
        }
    }

    private void validateSettings(RoomSettings settings) {
        if (settings.deckType() == DeckType.CUSTOM && settings.customDeck().isEmpty()) {
            throw new BadRequestException("Custom deck cannot be empty.");
        }

        if (settings.timerEnabled() && settings.timerDurationSeconds() < 30) {
            throw new BadRequestException("Timer duration must be at least 30 seconds.");
        }
    }
}
