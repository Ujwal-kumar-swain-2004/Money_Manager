package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SavingsGoalDTO {
    private Long id;
    private String name;
    private String icon;
    private BigDecimal targetAmount;
    private BigDecimal savedAmount;
    private LocalDate targetDate;
    private Double progressPercent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }
    public BigDecimal getSavedAmount() { return savedAmount; }
    public void setSavedAmount(BigDecimal savedAmount) { this.savedAmount = savedAmount; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public Double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Double progressPercent) { this.progressPercent = progressPercent; }
}
