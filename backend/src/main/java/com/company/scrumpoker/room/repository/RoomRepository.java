package com.company.scrumpoker.room.repository;

import com.company.scrumpoker.room.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
}
