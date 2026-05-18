package com.company.scrumpoker.websocket.dto;

public record SocketEvent<T>(
        SocketEventType type,
        T payload
) {
}
