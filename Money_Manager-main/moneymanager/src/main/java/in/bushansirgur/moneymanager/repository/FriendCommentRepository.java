package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FriendCommentRepository extends JpaRepository<FriendCommentEntity, Long> {
    List<FriendCommentEntity> findByExpenseIdOrderByCreatedAtDesc(Long expenseId);
    List<FriendCommentEntity> findByExpenseIdInOrderByCreatedAtDesc(Collection<Long> expenseIds);
}
