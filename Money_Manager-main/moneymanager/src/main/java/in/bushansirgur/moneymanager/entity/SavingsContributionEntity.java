package in.bushansirgur.moneymanager.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_savings_contributions")
public class SavingsContributionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private LocalDate contributionDate;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private SavingsGoalEntity goal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private ProfileEntity profile;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (contributionDate == null) contributionDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public SavingsGoalEntity getGoal() { return goal; }
    public void setGoal(SavingsGoalEntity goal) { this.goal = goal; }
    public ProfileEntity getProfile() { return profile; }
    public void setProfile(ProfileEntity profile) { this.profile = profile; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
