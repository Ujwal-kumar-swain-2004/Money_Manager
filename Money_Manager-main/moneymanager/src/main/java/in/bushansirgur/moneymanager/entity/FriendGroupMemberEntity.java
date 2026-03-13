package in.bushansirgur.moneymanager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tbl_friend_group_members")
public class FriendGroupMemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private FriendGroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private FriendEntity friend;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public FriendGroupEntity getGroup() { return group; }
    public void setGroup(FriendGroupEntity group) { this.group = group; }
    public FriendEntity getFriend() { return friend; }
    public void setFriend(FriendEntity friend) { this.friend = friend; }
}
