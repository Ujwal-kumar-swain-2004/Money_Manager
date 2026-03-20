package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.SharedExpenseSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedExpenseSplitRepository extends JpaRepository<SharedExpenseSplitEntity, Long> {
    List<SharedExpenseSplitEntity> findByExpenseProfileId(Long profileId);
    List<SharedExpenseSplitEntity> findByExpenseId(Long expenseId);
}
