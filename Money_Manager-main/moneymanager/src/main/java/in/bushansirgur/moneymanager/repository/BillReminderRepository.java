package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.BillReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillReminderRepository extends JpaRepository<BillReminderEntity, Long> {
    @Query("""
            select reminder from BillReminderEntity reminder
            left join fetch reminder.category
            where reminder.profile.id = :profileId
            order by reminder.dueDate asc
            """)
    List<BillReminderEntity> findByProfileIdOrderByDueDateAsc(@Param("profileId") Long profileId);

    @Query("""
            select reminder from BillReminderEntity reminder
            left join fetch reminder.category
            where reminder.profile.id = :profileId
            and reminder.paid = false
            and reminder.dueDate between :startDate and :endDate
            order by reminder.dueDate asc
            """)
    List<BillReminderEntity> findByProfileIdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(
            @Param("profileId") Long profileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<BillReminderEntity> findByIdAndProfileId(Long id, Long profileId);
}
