package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    List<BudgetEntity> findByProfileIdAndMonthAndYear(Long profileId, Integer month, Integer year);
    Optional<BudgetEntity> findByIdAndProfileId(Long id, Long profileId);
}
