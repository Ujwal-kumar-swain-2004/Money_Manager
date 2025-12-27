package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsContributionDTO {
    private Long id;
    private Long goalId;
    private BigDecimal amount;
    private LocalDate contributionDate;
    private String note;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getContributionDate() { return contributionDate; }
    public void setContributionDate(LocalDate contributionDate) { this.contributionDate = contributionDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
