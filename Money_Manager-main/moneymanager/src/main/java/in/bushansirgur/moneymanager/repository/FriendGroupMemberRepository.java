package in.bushansirgur.moneymanager.repository;

import in.bushansirgur.moneymanager.entity.FriendGroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FriendGroupMemberRepository extends JpaRepository<FriendGroupMemberEntity, Long> {
    @Query("""
            select member from FriendGroupMemberEntity member
            join fetch member.friend
            where member.group.id = :groupId
            """)
    List<FriendGroupMemberEntity> findByGroupId(Long groupId);

    @Query("""
            select member from FriendGroupMemberEntity member
            join fetch member.friend
            where member.group.id in :groupIds
            """)
    List<FriendGroupMemberEntity> findByGroupIdInWithFriend(@Param("groupIds") Collection<Long> groupIds);

    boolean existsByGroupIdAndFriendId(Long groupId, Long friendId);
}
