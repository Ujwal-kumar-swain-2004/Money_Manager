package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.SharedExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedExpenseRepository extends JpaRepository<SharedExpenseEntity, Long> {
    List<SharedExpenseEntity> findByProfileIdOrderByExpenseDateDesc(Long profileId);
    Optional<SharedExpenseEntity> findByIdAndProfileId(Long id, Long profileId);
}
