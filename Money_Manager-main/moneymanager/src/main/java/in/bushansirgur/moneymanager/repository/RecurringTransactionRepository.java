package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, Long> {
    List<RecurringTransactionEntity> findByProfileIdOrderByNextRunDateAsc(Long profileId);
    List<RecurringTransactionEntity> findByProfileIdAndActiveTrueAndNextRunDateLessThanEqual(Long profileId, LocalDate date);
    Optional<RecurringTransactionEntity> findByIdAndProfileId(Long id, Long profileId);
}
