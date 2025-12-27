package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.SavingsContributionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavingsContributionRepository extends JpaRepository<SavingsContributionEntity, Long> {
    List<SavingsContributionEntity> findByGoalIdAndProfileIdOrderByContributionDateDesc(Long goalId, Long profileId);
}
