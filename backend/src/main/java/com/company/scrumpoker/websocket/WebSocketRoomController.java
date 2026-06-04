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

import java.security.Principal;
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
            @Payload CastVoteRequest request,
            Principal principal
    ) {
        if (principal == null) {
            return;
        }
        RoomResponse response = votingService.castVote(roomId, request);
        roomEventPublisher.voteCast(roomId, response);
    }

    @MessageMapping("/rooms/{roomId}/reveal")
    public void reveal(@DestinationVariable UUID roomId, Principal principal) {
        if (principal == null) {
            return;
        }
        RoomResponse response = roomService.reveal(roomId, principal.getName());
        roomEventPublisher.votesRevealed(roomId, response);
    }

    @MessageMapping("/rooms/{roomId}/reset")
    public void reset(@DestinationVariable UUID roomId, Principal principal) {
        if (principal == null) {
            return;
        }
        RoomResponse response = roomService.reset(roomId, principal.getName());
        roomEventPublisher.votesReset(roomId, response);
    }
}
