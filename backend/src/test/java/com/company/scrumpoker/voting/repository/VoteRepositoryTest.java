package com.company.scrumpoker.voting.repository;

import com.company.scrumpoker.voting.entity.VoteEntity;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Scrum Poker")
@Feature("Votes persistence")
@DataJpaTest
class VoteRepositoryTest {

    @Autowired
    VoteRepository voteRepository;

    @Test
    @Story("Find vote")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Vote can be found by round and participant")
    void voteCanBeFoundByRoundAndParticipant() {
        UUID roomId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        VoteEntity saved = voteRepository.save(VoteEntity.builder()
                .id(UUID.randomUUID())
                .roomId(roomId)
                .roundId(roundId)
                .participantId(participantId)
                .value("8")
                .votedAt(Instant.now())
                .build());

        assertThat(voteRepository.findByRoundIdAndParticipantId(roundId, participantId))
                .isPresent()
                .get()
                .extracting(VoteEntity::getId, VoteEntity::getValue)
                .containsExactly(saved.getId(), "8");
    }
}
