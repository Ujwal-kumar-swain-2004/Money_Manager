package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.SharedExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface SharedExpenseRepository extends JpaRepository<SharedExpenseEntity, Long> {
    @Query("""
            select expense from SharedExpenseEntity expense
            left join fetch expense.paidByFriend
            left join fetch expense.group
            where expense.profile.id = :profileId
            order by expense.expenseDate desc
            """)
    List<SharedExpenseEntity> findByProfileIdOrderByExpenseDateDesc(@Param("profileId") Long profileId);

    Optional<SharedExpenseEntity> findByIdAndProfileId(Long id, Long profileId);
    long countByProfileIdAndExpenseDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);
    @Query("""
            select expense from SharedExpenseEntity expense
            left join fetch expense.paidByFriend
            left join fetch expense.group
            where expense.profile.id = :profileId
            and expense.group.id = :groupId
            """)
    List<SharedExpenseEntity> findByProfileIdAndGroupId(@Param("profileId") Long profileId, @Param("groupId") Long groupId);
}
