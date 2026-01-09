package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.BillReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillReminderRepository extends JpaRepository<BillReminderEntity, Long> {
    List<BillReminderEntity> findByProfileIdOrderByDueDateAsc(Long profileId);
    List<BillReminderEntity> findByProfileIdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(Long profileId, LocalDate startDate, LocalDate endDate);
    Optional<BillReminderEntity> findByIdAndProfileId(Long id, Long profileId);
}
