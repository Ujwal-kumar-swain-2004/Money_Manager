package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendActivityRepository extends JpaRepository<FriendActivityEntity, Long> {
    List<FriendActivityEntity> findTop25ByProfileIdOrderByCreatedAtDesc(Long profileId);
}
