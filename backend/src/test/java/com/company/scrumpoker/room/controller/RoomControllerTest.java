package com.company.scrumpoker.room.controller;

import com.company.scrumpoker.room.dto.CreateRoomRequest;
import com.company.scrumpoker.room.dto.CreateRoomResponse;
import com.company.scrumpoker.room.dto.JoinRoomRequest;
import com.company.scrumpoker.room.dto.JoinRoomResponse;
import com.company.scrumpoker.room.dto.RoomResponse;
import com.company.scrumpoker.room.model.ParticipantRole;
import com.company.scrumpoker.room.service.RoomService;
import com.company.scrumpoker.websocket.RoomEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Epic("Управление poker rooms")
@Feature("API для работы с комнатами")
@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    RoomService roomService;

    @Mock
    RoomEventPublisher roomEventPublisher;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(new RoomController(roomService, roomEventPublisher)).build();
    }

    @Test
    @Story("Создание комнаты")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Успешное создание комнаты через POST /api/rooms")
    void createRoom() throws Exception {
        //Подготовка данных
        UUID roomId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        var request = new CreateRoomRequest("Sprint Planning", "Alice");
        
        //Действие
        performCreateRoomRequest(roomId, participantId, request);
        
        //Проверка
        verify(roomService).create(request);
    }

    @Step("Выполнение POST-запроса на создание комнаты")
    private void performCreateRoomRequest(UUID roomId, UUID participantId, CreateRoomRequest request) throws Exception {
        when(roomService.create(request)).thenReturn(new CreateRoomResponse(roomId, participantId, "Планирование спринта"));

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId", is(roomId.toString())))
                .andExpect(jsonPath("$.participantId", is(participantId.toString())))
                .andExpect(jsonPath("$.roomName", is("Планирование спринта")));
    }

    @Test
    @Story("Подключение участников к комнате")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Успешное присоединение к комнате и публикация события POST /api/rooms/{roomId}/join")
    void joinRoomPublishesEvent() throws Exception {
        //Подготовка данных
        UUID roomId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        var request = new JoinRoomRequest("Bob", ParticipantRole.PARTICIPANT);
        RoomResponse roomResponse = org.mockito.Mockito.mock(RoomResponse.class);

        //Действие
        performJoinRoomRequest(roomId, participantId, request, roomResponse);

        //Проверка
        verifyEventPublication(roomId, roomResponse);
    }

    @Step("Выполнение POST-запроса на присоединение к комнате")
    private void performJoinRoomRequest(UUID roomId, UUID participantId, JoinRoomRequest request, RoomResponse roomResponse) throws Exception {
        when(roomService.join(roomId, request)).thenReturn(new JoinRoomResponse(roomId, participantId, "Bob"));
        when(roomService.getRoom(roomId)).thenReturn(roomResponse);

        mockMvc.perform(post("/api/rooms/{roomId}/join", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId", is(participantId.toString())))
                .andExpect(jsonPath("$.participantName", is("Bob")));
    }

    @Step("Проверка публикации события о присоединении участника")
    private void verifyEventPublication(UUID roomId, RoomResponse roomResponse) {
        verify(roomEventPublisher).participantJoined(roomId, roomResponse);
    }
}
