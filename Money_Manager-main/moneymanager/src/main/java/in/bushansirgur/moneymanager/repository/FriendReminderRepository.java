package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendReminderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendReminderRepository extends JpaRepository<FriendReminderEntity, Long> {
    @Query("""
            select reminder from FriendReminderEntity reminder
            join fetch reminder.friend
            where reminder.profile.id = :profileId
            order by reminder.dueDate asc
            """)
    List<FriendReminderEntity> findByProfileIdOrderByDueDateAsc(@Param("profileId") Long profileId);
}
