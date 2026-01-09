package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class MoneyPlanSummaryDTO {
    private List<BudgetAlertDTO> budgetAlerts;
    private CashflowForecastDTO cashflowForecast;

    public List<BudgetAlertDTO> getBudgetAlerts() { return budgetAlerts; }
    public void setBudgetAlerts(List<BudgetAlertDTO> budgetAlerts) { this.budgetAlerts = budgetAlerts; }
    public CashflowForecastDTO getCashflowForecast() { return cashflowForecast; }
    public void setCashflowForecast(CashflowForecastDTO cashflowForecast) { this.cashflowForecast = cashflowForecast; }

    public static class BudgetAlertDTO {
        private Long budgetId;
        private String categoryName;
        private BigDecimal budgetAmount;
        private BigDecimal spentAmount;
        private Double usagePercent;
        private String severity;
        private String message;

        public Long getBudgetId() { return budgetId; }
        public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public BigDecimal getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }
        public Double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class CashflowForecastDTO {
        private BigDecimal currentBalance;
        private BigDecimal projectedBalance;
        private BigDecimal projectedIncome;
        private BigDecimal projectedExpense;
        private LocalDate startDate;
        private LocalDate endDate;

        public BigDecimal getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
        public BigDecimal getProjectedBalance() { return projectedBalance; }
        public void setProjectedBalance(BigDecimal projectedBalance) { this.projectedBalance = projectedBalance; }
        public BigDecimal getProjectedIncome() { return projectedIncome; }
        public void setProjectedIncome(BigDecimal projectedIncome) { this.projectedIncome = projectedIncome; }
        public BigDecimal getProjectedExpense() { return projectedExpense; }
        public void setProjectedExpense(BigDecimal projectedExpense) { this.projectedExpense = projectedExpense; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}
