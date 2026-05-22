package com.company.scrumpoker.room.service;

import com.company.scrumpoker.common.exception.BadRequestException;
import com.company.scrumpoker.common.util.JsonUtils;
import com.company.scrumpoker.participant.entity.ParticipantEntity;
import com.company.scrumpoker.participant.repository.ParticipantRepository;
import com.company.scrumpoker.room.dto.CreateRoomRequest;
import com.company.scrumpoker.room.dto.JoinRoomRequest;
import com.company.scrumpoker.room.entity.RoomEntity;
import com.company.scrumpoker.room.mapper.RoomMapper;
import com.company.scrumpoker.room.model.ParticipantRole;
import com.company.scrumpoker.room.model.RoomSettings;
import com.company.scrumpoker.room.repository.RoomRepository;
import com.company.scrumpoker.voting.repository.VoteRepository;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Epic("Управление покер-румами")
@Feature("Бизнес-логика комнат (RoomService)")
@DisplayName("Тесты сервиса комнат")
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private JsonUtils jsonUtils;

    @InjectMocks
    private RoomService roomService;

    @Test
    @Story("Создание комнаты")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешное создание комнаты с назначением создателя модератором")
    void createRoom_shouldSaveRoomAndModerator() {
        var request = new CreateRoomRequest("Планирование спринта");
        String moderatorName = "test-moderator";
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(moderatorName, null, Collections.emptyList())
        );
        when(jsonUtils.toJson(any(RoomSettings.class))).thenReturn("{}");

        var response = roomService.create(request);

        assertThat(response.roomId()).isNotNull();
        assertThat(response.participantId()).isNotNull();
        assertThat(response.roomName()).isEqualTo("Планирование спринта");

        ArgumentCaptor<RoomEntity> roomCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        ArgumentCaptor<ParticipantEntity> participantCaptor = ArgumentCaptor.forClass(ParticipantEntity.class);

        verify(roomRepository).save(roomCaptor.capture());
        verify(participantRepository).save(participantCaptor.capture());

        assertThat(roomCaptor.getValue().getName()).isEqualTo("Планирование спринта");
        assertThat(participantCaptor.getValue().getName()).isEqualTo(moderatorName);
        assertThat(participantCaptor.getValue().getRole()).isEqualTo(ParticipantRole.MODERATOR);
    }

    @Test
    @Story("Проверка прав модератора")
    @DisplayName("Ошибка, если действие пытается выполнить не модератор")
    void requireModerator_whenUserIsNotModerator_shouldThrowException() {
        UUID roomId = UUID.randomUUID();
        String currentUsername = "some-user";
        
        var moderator = ParticipantEntity.builder().name("moderator-user").role(ParticipantRole.MODERATOR).build();
        var participant = ParticipantEntity.builder().name(currentUsername).role(ParticipantRole.PARTICIPANT).build();

        when(participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId)).thenReturn(List.of(moderator, participant));

        // ИСПРАВЛЕНО: Передаем имя пользователя явным аргументом
        assertThatThrownBy(() -> roomService.requireModerator(roomId, currentUsername))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only the moderator can perform this action.");
    }
    
    @Test
    @Story("Проверка прав модератора")
    @DisplayName("Успешная проверка, если действие выполняет модератор")
    void requireModerator_whenUserIsModerator_shouldNotThrowException() {
        UUID roomId = UUID.randomUUID();
        String currentUsername = "moderator-user";
        
        var moderator = ParticipantEntity.builder().name(currentUsername).role(ParticipantRole.MODERATOR).build();

        when(participantRepository.findByRoomIdOrderByJoinedAtAsc(roomId)).thenReturn(List.of(moderator));

        // ИСПРАВЛЕНО: Передаем имя пользователя явным аргументом. Если исключение не выброшено, тест пройдет.
        roomService.requireModerator(roomId, currentUsername);
    }

    @Test
    @Story("Присоединение к комнате")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка присоединения: наблюдатели запрещены настройками комнаты")
    void join_whenObserversDisabled_shouldThrowException() {
        UUID roomId = UUID.randomUUID();
        mockRoomWithDisabledObservers(roomId);

        JoinRoomRequest request = new JoinRoomRequest("Боб", ParticipantRole.OBSERVER);

        assertThatThrownBy(() -> roomService.join(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Observers are disabled");
    }

    private void mockRoomWithDisabledObservers(UUID roomId) {
        RoomEntity room = RoomEntity.builder().id(roomId).settingsJson("{}").build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(jsonUtils.fromJson("{}", RoomSettings.class))
                .thenReturn(new RoomSettings(null, java.util.List.of(), false, false, false, 60));
    }

    @Test
    @Story("Присоединение к комнате")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка присоединения: превышен лимит голосующих участников")
    void join_whenMaxVotersLimitReached_shouldThrowException() {
        UUID roomId = UUID.randomUUID();
        mockRoomWithMaxVotersLimitReached(roomId, 1);

        JoinRoomRequest request = new JoinRoomRequest("Чарли", ParticipantRole.PARTICIPANT);

        assertThatThrownBy(() -> roomService.join(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Room voting limit exceeded");
    }

    private void mockRoomWithMaxVotersLimitReached(UUID roomId, int maxVoters) {
        RoomEntity room = RoomEntity.builder().id(roomId).settingsJson("{}").build();
        ReflectionTestUtils.setField(roomService, "maxVoters", maxVoters);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(jsonUtils.fromJson("{}", RoomSettings.class)).thenReturn(RoomSettings.defaults());
        when(participantRepository.countByRoomIdAndRoleIn(any(), any())).thenReturn((long) maxVoters);
    }
}
