package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsDTO {
    private BigDecimal currentMonthExpense;
    private BigDecimal previousMonthExpense;
    private BigDecimal monthChangePercent;
    private List<CategoryBreakdownDTO> categoryBreakdown;
    private List<CalendarSpendDTO> calendarSpend;

    public BigDecimal getCurrentMonthExpense() { return currentMonthExpense; }
    public void setCurrentMonthExpense(BigDecimal currentMonthExpense) { this.currentMonthExpense = currentMonthExpense; }
    public BigDecimal getPreviousMonthExpense() { return previousMonthExpense; }
    public void setPreviousMonthExpense(BigDecimal previousMonthExpense) { this.previousMonthExpense = previousMonthExpense; }
    public BigDecimal getMonthChangePercent() { return monthChangePercent; }
    public void setMonthChangePercent(BigDecimal monthChangePercent) { this.monthChangePercent = monthChangePercent; }
    public List<CategoryBreakdownDTO> getCategoryBreakdown() { return categoryBreakdown; }
    public void setCategoryBreakdown(List<CategoryBreakdownDTO> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
    public List<CalendarSpendDTO> getCalendarSpend() { return calendarSpend; }
    public void setCalendarSpend(List<CalendarSpendDTO> calendarSpend) { this.calendarSpend = calendarSpend; }

    public static class CategoryBreakdownDTO {
        private Long categoryId;
        private String categoryName;
        private String icon;
        private BigDecimal amount;
        private Double percent;

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Double getPercent() { return percent; }
        public void setPercent(Double percent) { this.percent = percent; }
    }

    public static class CalendarSpendDTO {
        private LocalDate date;
        private BigDecimal amount;

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
