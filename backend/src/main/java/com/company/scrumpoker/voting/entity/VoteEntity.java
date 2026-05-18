package com.company.scrumpoker.voting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "votes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_votes_round_participant",
                        columnNames = {"roundId", "participantId"}
                )
        },
        indexes = {
                @Index(name = "idx_votes_room_round", columnList = "roomId,roundId")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoteEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private UUID roundId;

    @Column(nullable = false)
    private UUID participantId;

    @Column(name = "vote_value", nullable = false, length = 20)
    private String value;

    @Column(nullable = false)
    private Instant votedAt;
}
