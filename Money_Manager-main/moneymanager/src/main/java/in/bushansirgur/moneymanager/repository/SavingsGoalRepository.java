package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {
    List<SavingsGoalEntity> findByProfileIdOrderByTargetDateAsc(Long profileId);
    Optional<SavingsGoalEntity> findByIdAndProfileId(Long id, Long profileId);
}
