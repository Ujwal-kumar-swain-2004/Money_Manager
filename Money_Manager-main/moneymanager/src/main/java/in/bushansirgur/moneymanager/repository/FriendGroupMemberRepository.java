package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendGroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMemberEntity, Long> {
    List<FriendGroupMemberEntity> findByGroupId(Long groupId);
    boolean existsByGroupIdAndFriendId(Long groupId, Long friendId);
}
