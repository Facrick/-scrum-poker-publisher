package com.company.scrumpoker.room.entity;

import com.company.scrumpoker.room.model.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private UUID currentRoundId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoomStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String settingsJson;
}
