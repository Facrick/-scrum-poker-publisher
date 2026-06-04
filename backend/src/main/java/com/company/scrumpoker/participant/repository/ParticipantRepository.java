package com.company.scrumpoker.participant.repository;

import com.company.scrumpoker.participant.entity.ParticipantEntity;
import com.company.scrumpoker.room.model.ParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<ParticipantEntity, UUID> {

    List<ParticipantEntity> findByRoomIdOrderByJoinedAtAsc(UUID roomId);

    boolean existsByRoomIdAndRoleAndName(UUID roomId, ParticipantRole role, String name);

    long countByRoomIdAndRoleIn(UUID roomId, List<ParticipantRole> roles);
}
