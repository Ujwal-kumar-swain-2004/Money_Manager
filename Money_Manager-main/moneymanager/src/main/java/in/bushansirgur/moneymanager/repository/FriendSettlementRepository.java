package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendSettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendSettlementRepository extends JpaRepository<FriendSettlementEntity, Long> {
    List<FriendSettlementEntity> findByProfileIdOrderBySettlementDateDesc(Long profileId);
}
