package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.SharedExpenseSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SharedExpenseSplitRepository extends JpaRepository<SharedExpenseSplitEntity, Long> {
    @Query("""
            select split from SharedExpenseSplitEntity split
            join fetch split.friend
            join fetch split.expense expense
            left join fetch expense.paidByFriend
            where expense.profile.id = :profileId
            """)
    List<SharedExpenseSplitEntity> findByExpenseProfileId(@Param("profileId") Long profileId);

    @Query("""
            select split from SharedExpenseSplitEntity split
            join fetch split.friend
            where split.expense.id = :expenseId
            """)
    List<SharedExpenseSplitEntity> findByExpenseId(@Param("expenseId") Long expenseId);

    @Query("""
            select split from SharedExpenseSplitEntity split
            join fetch split.friend
            where split.expense.id in :expenseIds
            """)
    List<SharedExpenseSplitEntity> findByExpenseIdInWithFriend(@Param("expenseIds") Collection<Long> expenseIds);
}
