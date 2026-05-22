package com.company.scrumpoker.room.controller;

import com.company.scrumpoker.room.dto.*;
import com.company.scrumpoker.room.service.RoomService;
import com.company.scrumpoker.websocket.RoomEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public RoomResponse reveal(@PathVariable UUID roomId, Principal principal) {
        RoomResponse response = roomService.reveal(roomId, principal.getName());
        roomEventPublisher.votesRevealed(roomId, response);
        return response;
    }

    @PostMapping("/{roomId}/reset")
    public RoomResponse reset(@PathVariable UUID roomId, Principal principal) {
        RoomResponse response = roomService.reset(roomId, principal.getName());
        roomEventPublisher.votesReset(roomId, response);
        return response;
    }

    @PutMapping("/{roomId}/settings")
    public RoomResponse updateSettings(
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateRoomSettingsRequest request,
            Principal principal
    ) {
        RoomResponse response = roomService.updateSettings(roomId, request, principal.getName());
        roomEventPublisher.roomUpdated(roomId, response);
        return response;
    }
}
