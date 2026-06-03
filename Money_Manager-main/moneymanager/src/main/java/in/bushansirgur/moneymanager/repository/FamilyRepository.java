package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FamilyRepository extends JpaRepository<FamilyEntity, Long> {
    List<FamilyEntity> findByOwnerId(Long ownerId);
    Optional<FamilyEntity> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<FamilyEntity> findByInviteCode(String inviteCode);
}
