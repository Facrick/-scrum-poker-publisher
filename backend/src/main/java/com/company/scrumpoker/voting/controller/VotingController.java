package com.company.scrumpoker.voting.controller;

import com.company.scrumpoker.room.dto.RoomResponse;
import com.company.scrumpoker.voting.dto.CastVoteRequest;
import com.company.scrumpoker.voting.service.VotingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/rooms/{roomId}/votes")
@RequiredArgsConstructor
public class VotingController {

    private final VotingService votingService;

    @PostMapping
    public RoomResponse castVote(
            @PathVariable UUID roomId,
            @Valid @RequestBody CastVoteRequest request
    ) {
        return votingService.castVote(roomId, request);
    }
}
