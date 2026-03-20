package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<FriendEntity, Long> {
    List<FriendEntity> findByProfileIdOrderByNameAsc(Long profileId);
    Optional<FriendEntity> findByIdAndProfileId(Long id, Long profileId);
    Optional<FriendEntity> findByInviteCodeAndProfileId(String inviteCode, Long profileId);
}
