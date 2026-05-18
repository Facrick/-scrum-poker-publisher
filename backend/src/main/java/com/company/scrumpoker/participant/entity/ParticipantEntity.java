package com.company.scrumpoker.participant.entity;

import com.company.scrumpoker.room.model.ParticipantRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "participants",
        indexes = {
                @Index(name = "idx_participants_room_id", columnList = "roomId")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false, length = 80)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ParticipantRole role;

    @Column(nullable = false)
    private boolean connected;

    @Column(nullable = false)
    private Instant joinedAt;
}
