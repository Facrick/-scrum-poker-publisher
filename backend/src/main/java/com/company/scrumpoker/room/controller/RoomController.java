package com.company.scrumpoker.room.controller;

import com.company.scrumpoker.room.dto.*;
import com.company.scrumpoker.room.service.RoomService;
import com.company.scrumpoker.websocket.RoomEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomEventPublisher roomEventPublisher;

    @PostMapping
    public CreateRoomResponse create(@Valid @RequestBody CreateRoomRequest request) {
        return roomService.create(request);
    }

    @PostMapping("/{roomId}/join")
    public JoinRoomResponse join(
            @PathVariable UUID roomId,
            @Valid @RequestBody JoinRoomRequest request
    ) {
        JoinRoomResponse joinResponse = roomService.join(roomId, request);
        RoomResponse roomResponse = roomService.getRoom(roomId);

        roomEventPublisher.participantJoined(roomId, roomResponse);

        return joinResponse;
    }

    @GetMapping("/{roomId}")
    public RoomResponse get(@PathVariable UUID roomId) {
        return roomService.getRoom(roomId);
    }

    @PostMapping("/{roomId}/reveal")
    public RoomResponse reveal(
            @PathVariable UUID roomId,
            @RequestParam UUID moderatorId
    ) {
        RoomResponse response = roomService.reveal(roomId, moderatorId);
        roomEventPublisher.votesRevealed(roomId, response);
        return response;
    }

    @PostMapping("/{roomId}/reset")
    public RoomResponse reset(
            @PathVariable UUID roomId,
            @RequestParam UUID moderatorId
    ) {
        RoomResponse response = roomService.reset(roomId, moderatorId);
        roomEventPublisher.votesReset(roomId, response);
        return response;
    }

    @PutMapping("/{roomId}/settings")
    public RoomResponse updateSettings(
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomSettingsRequest request
    ) {
        RoomResponse response = roomService.updateSettings(roomId, request);
        roomEventPublisher.roomUpdated(roomId, response);
        return response;
    }
}
