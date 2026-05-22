package com.company.scrumpoker.room.controller;

import com.company.scrumpoker.config.ApplicationConfig;
import com.company.scrumpoker.config.SecurityConfig;
import com.company.scrumpoker.jwt.JwtService;
import com.company.scrumpoker.room.dto.CreateRoomRequest;
import com.company.scrumpoker.room.dto.CreateRoomResponse;
import com.company.scrumpoker.room.dto.JoinRoomRequest;
import com.company.scrumpoker.room.dto.JoinRoomResponse;
import com.company.scrumpoker.room.model.ParticipantRole;
import com.company.scrumpoker.room.service.RoomService;
import com.company.scrumpoker.user.repository.UserRepository; // Импортируем недостающий класс
import com.company.scrumpoker.user.service.AuthService;
import com.company.scrumpoker.websocket.RoomEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class, ApplicationConfig.class})
@DisplayName("API для работы с комнатами")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private RoomEventPublisher roomEventPublisher;

    @MockBean
    private JwtService jwtService;
    
    // Добавляем недостающий мок для UserRepository
    @MockBean
    private UserRepository userRepository;


    @Test
    @DisplayName("Успешное создание комнаты авторизованным пользователем")
    @WithMockUser(username = "test-moderator")
    void createRoom_whenAuthorized_shouldSucceed() throws Exception {
        var request = new CreateRoomRequest("Sprint Planning");
        var response = new CreateRoomResponse(UUID.randomUUID(), UUID.randomUUID(), "Sprint Planning");
        when(roomService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId", is(response.roomId().toString())));

        verify(roomService).create(request);
    }

    @Test
    @DisplayName("Успешное присоединение к комнате и публикация события")
    void joinRoom_shouldSucceedAndPublishEvent() throws Exception {
        UUID roomId = UUID.randomUUID();
        var request = new JoinRoomRequest("Bob", ParticipantRole.PARTICIPANT);
        var joinResponse = new JoinRoomResponse(roomId, UUID.randomUUID(), "Bob");
        var roomResponse = org.mockito.Mockito.mock(com.company.scrumpoker.room.dto.RoomResponse.class);

        when(roomService.join(roomId, request)).thenReturn(joinResponse);
        when(roomService.getRoom(roomId)).thenReturn(roomResponse);

        mockMvc.perform(post("/api/rooms/{roomId}/join", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId", is(joinResponse.participantId().toString())));

        verify(roomEventPublisher).participantJoined(roomId, roomResponse);
    }

    @Test
    @DisplayName("Успешное вскрытие карт модератором")
    @WithMockUser(username = "test-moderator")
    void revealRoom_shouldSucceed() throws Exception {
        UUID roomId = UUID.randomUUID();
        String username = "test-moderator";
        var roomResponse = org.mockito.Mockito.mock(com.company.scrumpoker.room.dto.RoomResponse.class);
        
        when(roomService.reveal(eq(roomId), eq(username))).thenReturn(roomResponse);

        mockMvc.perform(post("/api/rooms/{roomId}/reveal", roomId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(roomService).reveal(eq(roomId), eq(username));
        verify(roomEventPublisher).votesRevealed(roomId, roomResponse);
    }

    @Test
    @DisplayName("Успешный сброс голосов модератором")
    @WithMockUser(username = "test-moderator")
    void resetRoom_shouldSucceed() throws Exception {
        UUID roomId = UUID.randomUUID();
        String username = "test-moderator";
        var roomResponse = org.mockito.Mockito.mock(com.company.scrumpoker.room.dto.RoomResponse.class);
        
        when(roomService.reset(eq(roomId), eq(username))).thenReturn(roomResponse);

        mockMvc.perform(post("/api/rooms/{roomId}/reset", roomId)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(roomService).reset(eq(roomId), eq(username));
        verify(roomEventPublisher).votesReset(roomId, roomResponse);
    }
}
