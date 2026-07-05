package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.AiChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessageEntity, Long> {
    List<AiChatMessageEntity> findTop20ByProfileIdOrderByCreatedAtDesc(Long profileId);
    List<AiChatMessageEntity> findTop50ByProfileIdOrderByCreatedAtDesc(Long profileId);
    void deleteByProfileId(Long profileId);
}
