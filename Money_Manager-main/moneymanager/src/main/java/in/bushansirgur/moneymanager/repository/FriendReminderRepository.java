package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendReminderRepository extends JpaRepository<FriendReminderEntity, Long> {
    List<FriendReminderEntity> findByProfileIdOrderByDueDateAsc(Long profileId);
}
