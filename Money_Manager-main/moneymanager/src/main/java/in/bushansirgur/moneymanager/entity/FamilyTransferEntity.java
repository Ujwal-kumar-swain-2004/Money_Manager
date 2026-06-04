package in.bushansirgur.moneymanager.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_family_transfers", indexes = {
        @Index(name = "idx_family_transfer_family_date", columnList = "family_id,transfer_date"),
        @Index(name = "idx_family_transfer_from_member", columnList = "from_member_id"),
        @Index(name = "idx_family_transfer_to_member", columnList = "to_member_id")
})
public class FamilyTransferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private LocalDate transferDate;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private FamilyEntity family;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id")
    private FamilyMemberEntity fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id")
    private FamilyMemberEntity toMember;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (transferDate == null) transferDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public FamilyEntity getFamily() { return family; }
    public void setFamily(FamilyEntity family) { this.family = family; }
    public FamilyMemberEntity getFromMember() { return fromMember; }
    public void setFromMember(FamilyMemberEntity fromMember) { this.fromMember = fromMember; }
    public FamilyMemberEntity getToMember() { return toMember; }
    public void setToMember(FamilyMemberEntity toMember) { this.toMember = toMember; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
