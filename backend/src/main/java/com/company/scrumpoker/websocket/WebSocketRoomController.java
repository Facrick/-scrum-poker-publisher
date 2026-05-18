package com.company.scrumpoker.websocket;

import com.company.scrumpoker.room.dto.RoomResponse;
import com.company.scrumpoker.room.service.RoomService;
import com.company.scrumpoker.voting.dto.CastVoteRequest;
import com.company.scrumpoker.voting.service.VotingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketRoomController {

    private final VotingService votingService;
    private final RoomService roomService;
    private final RoomEventPublisher roomEventPublisher;

    @MessageMapping("/rooms/{roomId}/vote")
    public void vote(
            @DestinationVariable UUID roomId,
            @Payload CastVoteRequest request
    ) {
        RoomResponse response = votingService.castVote(roomId, request);
        roomEventPublisher.voteCast(roomId, response);
    }

    @MessageMapping("/rooms/{roomId}/reveal")
    public void reveal(
            @DestinationVariable UUID roomId,
            @Payload Map<String, String> payload
    ) {
        UUID moderatorId = UUID.fromString(payload.get("moderatorId"));
        RoomResponse response = roomService.reveal(roomId, moderatorId);
        roomEventPublisher.votesRevealed(roomId, response);
    }

    @MessageMapping("/rooms/{roomId}/reset")
    public void reset(
            @DestinationVariable UUID roomId,
            @Payload Map<String, String> payload
    ) {
        UUID moderatorId = UUID.fromString(payload.get("moderatorId"));
        RoomResponse response = roomService.reset(roomId, moderatorId);
        roomEventPublisher.votesReset(roomId, response);
    }
}
