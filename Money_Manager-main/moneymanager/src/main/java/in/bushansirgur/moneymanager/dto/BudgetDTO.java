package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;

public class BudgetDTO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private Integer month;
    private Integer year;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private Double usagePercent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getSpent() { return spent; }
    public void setSpent(BigDecimal spent) { this.spent = spent; }
    public BigDecimal getRemaining() { return remaining; }
    public void setRemaining(BigDecimal remaining) { this.remaining = remaining; }
    public Double getUsagePercent() { return usagePercent; }
    public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
}
