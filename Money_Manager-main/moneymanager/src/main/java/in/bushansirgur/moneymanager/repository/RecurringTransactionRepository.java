package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, Long> {
    @Query("""
            select recurring from RecurringTransactionEntity recurring
            join fetch recurring.category
            where recurring.profile.id = :profileId
            order by recurring.nextRunDate asc
            """)
    List<RecurringTransactionEntity> findByProfileIdOrderByNextRunDateAsc(@Param("profileId") Long profileId);

    @Query("""
            select recurring from RecurringTransactionEntity recurring
            join fetch recurring.category
            where recurring.profile.id = :profileId
            and recurring.active = true
            and recurring.nextRunDate <= :date
            order by recurring.nextRunDate asc
            """)
    List<RecurringTransactionEntity> findByProfileIdAndActiveTrueAndNextRunDateLessThanEqual(@Param("profileId") Long profileId, @Param("date") LocalDate date);

    Optional<RecurringTransactionEntity> findByIdAndProfileId(Long id, Long profileId);
}
