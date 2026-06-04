package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendSettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendSettlementRepository extends JpaRepository<FriendSettlementEntity, Long> {
    @Query("""
            select settlement from FriendSettlementEntity settlement
            join fetch settlement.friend
            where settlement.profile.id = :profileId
            order by settlement.settlementDate desc
            """)
    List<FriendSettlementEntity> findByProfileIdOrderBySettlementDateDesc(@Param("profileId") Long profileId);
}
