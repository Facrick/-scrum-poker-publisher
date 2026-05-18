package com.company.scrumpoker.websocket;

import com.company.scrumpoker.room.dto.RoomResponse;
import com.company.scrumpoker.websocket.dto.SocketEvent;
import com.company.scrumpoker.websocket.dto.SocketEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoomEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(UUID roomId, SocketEventType type, RoomResponse response) {
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + roomId,
                new SocketEvent<>(type, response)
        );
    }

    public void roomUpdated(UUID roomId, RoomResponse response) {
        publish(roomId, SocketEventType.ROOM_UPDATED, response);
    }

    public void participantJoined(UUID roomId, RoomResponse response) {
        publish(roomId, SocketEventType.PARTICIPANT_JOINED, response);
    }

    public void votesRevealed(UUID roomId, RoomResponse response) {
        publish(roomId, SocketEventType.VOTES_REVEALED, response);
    }

    public void votesReset(UUID roomId, RoomResponse response) {
        publish(roomId, SocketEventType.VOTES_RESET, response);
    }

    public void voteCast(UUID roomId, RoomResponse response) {
        publish(roomId, SocketEventType.VOTE_CAST, response);
    }
}
