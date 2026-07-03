package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.RecurringSharedExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecurringSharedExpenseRepository extends JpaRepository<RecurringSharedExpenseEntity, Long> {
    @Query("""
            select recurring from RecurringSharedExpenseEntity recurring
            left join fetch recurring.group
            left join fetch recurring.paidByFriend
            where recurring.profile.id = :profileId
            order by recurring.nextDueDate asc
            """)
    List<RecurringSharedExpenseEntity> findByProfileIdOrderByNextDueDateAsc(@Param("profileId") Long profileId);

    @Query("""
            select recurring from RecurringSharedExpenseEntity recurring
            left join fetch recurring.group
            left join fetch recurring.paidByFriend
            where recurring.profile.id = :profileId
              and recurring.active = true
              and recurring.nextDueDate <= :date
            order by recurring.nextDueDate asc
            """)
    List<RecurringSharedExpenseEntity> findDueByProfileId(@Param("profileId") Long profileId, @Param("date") LocalDate date);
}
