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
import com.company.scrumpoker.voting.dto.VoteStatsResponse;
import com.company.scrumpoker.voting.entity.VoteEntity;
import com.company.scrumpoker.voting.repository.VoteRepository;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Epic("Бизнес-логика")
@Feature("Голосование")
@DisplayName("Тесты сервиса голосования (VotingService)")
@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private VotingService votingService;

    private UUID roomId;
    private UUID roundId;
    private UUID participantId;
    private RoomEntity room;
    private ParticipantEntity participant;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        roundId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        room = RoomEntity.builder()
                .id(roomId)
                .currentRoundId(roundId)
                .status(RoomStatus.VOTING)
                .settingsJson("{}")
                .build();

        participant = ParticipantEntity.builder()
                .id(participantId)
                .roomId(roomId)
                .role(ParticipantRole.PARTICIPANT)
                .name("Bob")
                .build();
    }

    @Test
    @Story("Прием голоса")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешный прием голоса от участника")
    void castVote_whenAllConditionsMet_shouldSaveVote() {
        // Arrange
        var request = new CastVoteRequest(participantId, "5");
        var deck = List.of("1", "3", "5", "8");
        var expectedResponse = mock(RoomResponse.class);

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        when(roomService.getSettings(room)).thenReturn(RoomSettings.defaults());
        when(roomService.resolveDeck(any())).thenReturn(deck);
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(voteRepository.findByRoundIdAndParticipantId(roundId, participantId)).thenReturn(Optional.empty());
        when(roomService.buildRoomResponse(room)).thenReturn(expectedResponse);

        // Act
        RoomResponse actualResponse = votingService.castVote(roomId, request);

        // Assert
        assertThat(actualResponse).isSameAs(expectedResponse);

        var voteCaptor = ArgumentCaptor.forClass(VoteEntity.class);
        verify(voteRepository).save(voteCaptor.capture());

        VoteEntity savedVote = voteCaptor.getValue();
        assertThat(savedVote.getValue()).isEqualTo("5");
        assertThat(savedVote.getParticipantId()).isEqualTo(participantId);
    }

    @Test
    @Story("Прием голоса")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка: Попытка проголосовать после вскрытия карт")
    void castVote_whenRoomIsRevealed_shouldThrowException() {
        // Arrange
        room.setStatus(RoomStatus.REVEALED);
        var request = new CastVoteRequest(participantId, "5");

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        // Другие моки не нужны, т.к. метод выйдет раньше

        // Act & Assert
        assertThatThrownBy(() -> votingService.castVote(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Voting is already revealed");
    }

    @Test
    @Story("Прием голоса")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка: Попытка проголосовать картой не из колоды")
    void castVote_whenValueNotInDeck_shouldThrowException() {
        // Arrange
        var request = new CastVoteRequest(participantId, "99"); // "99" нет в колоде

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        when(roomService.getSettings(room)).thenReturn(RoomSettings.defaults());
        when(roomService.resolveDeck(any())).thenReturn(List.of("1", "2", "3"));
        // Мок participantRepository не нужен

        // Act & Assert
        assertThatThrownBy(() -> votingService.castVote(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Unsupported vote value");
    }

    @Test
    @Story("Прием голоса")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка: Участник не найден")
    void castVote_whenParticipantNotFound_shouldThrowException() {
        // Arrange
        var request = new CastVoteRequest(UUID.randomUUID(), "5"); // Несуществующий ID

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        when(roomService.getSettings(room)).thenReturn(RoomSettings.defaults());
        when(roomService.resolveDeck(any())).thenReturn(List.of("1", "5", "8"));
        when(participantRepository.findById(request.participantId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> votingService.castVote(roomId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Participant not found");
    }

    @Test
    @Story("Прием голоса")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка: Участник не принадлежит к данной комнате")
    void castVote_whenParticipantNotInRoom_shouldThrowException() {
        // Arrange
        participant.setRoomId(UUID.randomUUID()); // Участник из другой комнаты
        var request = new CastVoteRequest(participantId, "5");

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        when(roomService.getSettings(room)).thenReturn(RoomSettings.defaults());
        when(roomService.resolveDeck(any())).thenReturn(List.of("5"));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Act & Assert
        assertThatThrownBy(() -> votingService.castVote(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Participant does not belong to this room");
    }

    @Test
    @Story("Прием голоса")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Ошибка: Наблюдатель не может голосовать")
    void castVote_whenParticipantIsObserver_shouldThrowException() {
        // Arrange
        participant.setRole(ParticipantRole.OBSERVER);
        var request = new CastVoteRequest(participantId, "5");

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        when(roomService.getSettings(room)).thenReturn(RoomSettings.defaults());
        when(roomService.resolveDeck(any())).thenReturn(List.of("5"));
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));

        // Act & Assert
        assertThatThrownBy(() -> votingService.castVote(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Observers cannot vote");
    }

    @Test
    @Story("Авто-вскрытие карт")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Карты автоматически вскрываются, если проголосовали все участники")
    void castVote_whenAutoRevealEnabledAndAllVoted_shouldRevealRoom() {
        // Arrange
        var settings = new RoomSettings(null, null, true, false, false, 0); // autoReveal = true
        var request = new CastVoteRequest(participantId, "8");
        var deck = List.of("8");

        var statsBeforeVote = new VoteStatsResponse(10, 9, null, null, null, false);
        var responseBeforeVote = new RoomResponse(roomId, "Room", roundId, RoomStatus.VOTING, 100, settings, deck, statsBeforeVote, List.of());

        var statsAfterVote = new VoteStatsResponse(10, 10, null, null, null, true);
        var responseAfterVote = new RoomResponse(roomId, "Room", roundId, RoomStatus.REVEALED, 100, settings, deck, statsAfterVote, List.of());

        when(roomService.getRoomEntity(roomId)).thenReturn(room);
        when(roomService.getSettings(room)).thenReturn(settings);
        when(roomService.resolveDeck(settings)).thenReturn(deck);
        when(participantRepository.findById(participantId)).thenReturn(Optional.of(participant));
        when(voteRepository.findByRoundIdAndParticipantId(roundId, participantId)).thenReturn(Optional.empty());

        when(roomService.buildRoomResponse(room))
                .thenReturn(responseBeforeVote)
                .thenReturn(responseAfterVote);

        // Act
        votingService.castVote(roomId, request);

        // Assert
        var roomCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        verify(roomService, times(2)).buildRoomResponse(roomCaptor.capture());
        assertThat(roomCaptor.getValue().getStatus()).isEqualTo(RoomStatus.REVEALED);
    }
}