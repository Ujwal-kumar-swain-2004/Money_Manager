package in.bushansirgur.moneymanager.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_recurring_shared_expenses", indexes = {
        @Index(name = "idx_recurring_shared_profile_next_due", columnList = "profile_id,next_due_date"),
        @Index(name = "idx_recurring_shared_active_due", columnList = "active,next_due_date"),
        @Index(name = "idx_recurring_shared_group", columnList = "group_id")
})
public class RecurringSharedExpenseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private String splitType;
    private String frequency;
    private LocalDate nextDueDate;
    private Boolean active;
    private String note;
    private String receiptUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by_friend_id")
    private FriendEntity paidByFriend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private FriendGroupEntity group;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (active == null) active = true;
        if (splitType == null || splitType.isBlank()) splitType = "equal";
        if (frequency == null || frequency.isBlank()) frequency = "monthly";
        if (nextDueDate == null) nextDueDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSplitType() { return splitType; }
    public void setSplitType(String splitType) { this.splitType = splitType; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getReceiptUrl() { return receiptUrl; }
    public void setReceiptUrl(String receiptUrl) { this.receiptUrl = receiptUrl; }
    public ProfileEntity getProfile() { return profile; }
    public void setProfile(ProfileEntity profile) { this.profile = profile; }
    public FriendEntity getPaidByFriend() { return paidByFriend; }
    public void setPaidByFriend(FriendEntity paidByFriend) { this.paidByFriend = paidByFriend; }
    public FriendGroupEntity getGroup() { return group; }
    public void setGroup(FriendGroupEntity group) { this.group = group; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
