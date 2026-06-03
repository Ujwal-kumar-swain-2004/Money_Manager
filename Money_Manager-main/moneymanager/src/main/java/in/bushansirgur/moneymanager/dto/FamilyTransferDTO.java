package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FamilyTransferDTO {
    private Long id;
    private Long familyId;
    private Long fromMemberId;
    private String fromMemberName;
    private Long toMemberId;
    private String toMemberName;
    private BigDecimal amount;
    private LocalDate transferDate;
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFamilyId() { return familyId; }
    public void setFamilyId(Long familyId) { this.familyId = familyId; }
    public Long getFromMemberId() { return fromMemberId; }
    public void setFromMemberId(Long fromMemberId) { this.fromMemberId = fromMemberId; }
    public String getFromMemberName() { return fromMemberName; }
    public void setFromMemberName(String fromMemberName) { this.fromMemberName = fromMemberName; }
    public Long getToMemberId() { return toMemberId; }
    public void setToMemberId(Long toMemberId) { this.toMemberId = toMemberId; }
    public String getToMemberName() { return toMemberName; }
    public void setToMemberName(String toMemberName) { this.toMemberName = toMemberName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDate transferDate) { this.transferDate = transferDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
