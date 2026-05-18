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
import org.springframework.test.util.ReflectionTestUtils;

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
    RoomRepository roomRepository;

    @Mock
    ParticipantRepository participantRepository;

    @Mock
    VoteRepository voteRepository;

    @Mock
    RoomMapper roomMapper;

    @Mock
    JsonUtils jsonUtils;

    @InjectMocks
    RoomService roomService;

    @Test
    @Story("Создание комнаты")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешное создание комнаты с назначением создателя модератором")
    void createRoomSavesRoomAndModerator() {
        // 1. Подготовка данных (Arrange)
        CreateRoomRequest request = new CreateRoomRequest("Планирование спринта", "Алиса");
        mockJsonSerialization();

        // 2. Выполнение действия (Act)
        var response = performCreateRoom(request);

        // 3. Проверка результата (Assert)
        verifyRoomCreationResponse(response, "Планирование спринта");
        verifyEntitiesSavedToDatabase("Планирование спринта", "Алиса");
    }

    @Step("Мокирование сериализации настроек в JSON")
    private void mockJsonSerialization() {
        when(jsonUtils.toJson(any(RoomSettings.class))).thenReturn("{}");
    }

    @Step("Вызов метода создания комнаты")
    private com.company.scrumpoker.room.dto.CreateRoomResponse performCreateRoom(CreateRoomRequest request) {
        return roomService.create(request);
    }

    @Step("Проверка ответа после создания комнаты")
    private void verifyRoomCreationResponse(com.company.scrumpoker.room.dto.CreateRoomResponse response, String expectedRoomName) {
        assertThat(response.roomId()).isNotNull();
        assertThat(response.participantId()).isNotNull();
        assertThat(response.roomName()).isEqualTo(expectedRoomName);
    }

    @Step("Проверка, что комната и участник(модератор) сохранены в базу данных")
    private void verifyEntitiesSavedToDatabase(String expectedRoomName, String expectedModeratorName) {
        ArgumentCaptor<RoomEntity> roomCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        ArgumentCaptor<ParticipantEntity> participantCaptor = ArgumentCaptor.forClass(ParticipantEntity.class);

        verify(roomRepository).save(roomCaptor.capture());
        verify(participantRepository).save(participantCaptor.capture());

        assertThat(roomCaptor.getValue().getName()).isEqualTo(expectedRoomName);
        assertThat(participantCaptor.getValue().getName()).isEqualTo(expectedModeratorName);
        assertThat(participantCaptor.getValue().getRole()).isEqualTo(ParticipantRole.MODERATOR);
        assertThat(participantCaptor.getValue().getRoomId()).isEqualTo(roomCaptor.getValue().getId());
    }

    @Test
    @Story("Присоединение к комнате")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка присоединения: наблюдатели запрещены настройками комнаты")
    void observerCannotJoinWhenObserversDisabled() {
        UUID roomId = UUID.randomUUID();
        mockRoomWithDisabledObservers(roomId);

        JoinRoomRequest request = new JoinRoomRequest("Боб", ParticipantRole.OBSERVER);

        assertCannotJoinDueToObserversDisabled(roomId, request);
    }

    @Step("Подготовка: комната, где наблюдатели запрещены")
    private void mockRoomWithDisabledObservers(UUID roomId) {
        RoomEntity room = RoomEntity.builder()
                .id(roomId)
                .settingsJson("{}")
                .build();

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(jsonUtils.fromJson("{}", RoomSettings.class))
                .thenReturn(new RoomSettings(null, java.util.List.of(), false, false, false, 60)); // observersAllowed = false
    }

    @Step("Попытка присоединиться и проверка выброса ошибки 'Observers are disabled'")
    private void assertCannotJoinDueToObserversDisabled(UUID roomId, JoinRoomRequest request) {
        assertThatThrownBy(() -> roomService.join(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Observers are disabled");
    }

    @Test
    @Story("Присоединение к комнате")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Ошибка присоединения: превышен лимит голосующих участников")
    void participantCannotJoinWhenMaxVotersLimitReached() {
        UUID roomId = UUID.randomUUID();
        mockRoomWithMaxVotersLimitReached(roomId, 1);

        JoinRoomRequest request = new JoinRoomRequest("Чарли", ParticipantRole.PARTICIPANT);

        assertCannotJoinDueToVotersLimit(roomId, request);
    }

    @Step("Подготовка: комната, в которой уже достигнут лимит {maxVoters} участников")
    private void mockRoomWithMaxVotersLimitReached(UUID roomId, int maxVoters) {
        RoomEntity room = RoomEntity.builder()
                .id(roomId)
                .settingsJson("{}")
                .build();

        ReflectionTestUtils.setField(roomService, "maxVoters", maxVoters);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(jsonUtils.fromJson("{}", RoomSettings.class)).thenReturn(RoomSettings.defaults());
        
        // Мокируем, что в базе уже есть участники (их количество равно лимиту)
        when(participantRepository.countByRoomIdAndRoleIn(any(), any())).thenReturn((long) maxVoters);
    }

    @Step("Попытка присоединиться и проверка выброса ошибки 'Room voting limit exceeded'")
    private void assertCannotJoinDueToVotersLimit(UUID roomId, JoinRoomRequest request) {
        assertThatThrownBy(() -> roomService.join(roomId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Room voting limit exceeded");
    }
}