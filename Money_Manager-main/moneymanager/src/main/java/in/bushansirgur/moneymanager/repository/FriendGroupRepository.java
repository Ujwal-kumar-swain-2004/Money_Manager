package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendGroupRepository extends JpaRepository<FriendGroupEntity, Long> {
    List<FriendGroupEntity> findByProfileIdOrderByIdDesc(Long profileId);
    Optional<FriendGroupEntity> findByIdAndProfileId(Long id, Long profileId);
    long countByProfileId(Long profileId);
}
