package com.company.scrumpoker.websocket.dto;

public enum SocketEventType {
    ROOM_UPDATED,
    VOTE_CAST,
    VOTES_REVEALED,
    VOTES_RESET,
    PARTICIPANT_JOINED,
    PARTICIPANT_LEFT,
    ERROR,
    PONG
}
