package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    @Query("""
            select b
            from BudgetEntity b
            join fetch b.category
            join fetch b.profile
            where b.profile.id = :profileId
              and b.month = :month
              and b.year = :year
            """)
    List<BudgetEntity> findByProfileIdAndMonthAndYear(
            @Param("profileId") Long profileId,
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    Optional<BudgetEntity> findByIdAndProfileId(Long id, Long profileId);
}
