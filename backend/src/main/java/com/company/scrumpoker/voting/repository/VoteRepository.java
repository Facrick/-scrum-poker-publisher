package com.company.scrumpoker.voting.repository;

import com.company.scrumpoker.voting.entity.VoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<VoteEntity, UUID> {

    List<VoteEntity> findByRoomIdAndRoundId(UUID roomId, UUID roundId);

    Optional<VoteEntity> findByRoundIdAndParticipantId(UUID roundId, UUID participantId);

    void deleteByRoomIdAndRoundId(UUID roomId, UUID roundId);
}
