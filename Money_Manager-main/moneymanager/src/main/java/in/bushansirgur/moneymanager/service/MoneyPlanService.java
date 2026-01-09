package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.BudgetDTO;
import in.bushansirgur.moneymanager.dto.MoneyPlanSummaryDTO;
import in.bushansirgur.moneymanager.entity.BillReminderEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.entity.RecurringTransactionEntity;
import in.bushansirgur.moneymanager.repository.BillReminderRepository;
import in.bushansirgur.moneymanager.repository.RecurringTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MoneyPlanService {
    @Autowired private BudgetService budgetService;
    @Autowired private IncomeService incomeService;
    @Autowired private ExpenseService expenseService;
    @Autowired private ProfileService profileService;
    @Autowired private RecurringTransactionRepository recurringTransactionRepository;
    @Autowired private BillReminderRepository billReminderRepository;

    public MoneyPlanSummaryDTO summary(Integer month, Integer year, Integer forecastDays) {
        MoneyPlanSummaryDTO dto = new MoneyPlanSummaryDTO();
        dto.setBudgetAlerts(buildBudgetAlerts(month, year));
        dto.setCashflowForecast(buildForecast(forecastDays == null ? 30 : forecastDays));
        return dto;
    }

    private List<MoneyPlanSummaryDTO.BudgetAlertDTO> buildBudgetAlerts(Integer month, Integer year) {
        List<MoneyPlanSummaryDTO.BudgetAlertDTO> alerts = new ArrayList<>();
        for (BudgetDTO budget : budgetService.list(month, year)) {
            double usage = budget.getUsagePercent() == null ? 0 : budget.getUsagePercent();
            if (usage < 70) continue;
            MoneyPlanSummaryDTO.BudgetAlertDTO alert = new MoneyPlanSummaryDTO.BudgetAlertDTO();
            alert.setBudgetId(budget.getId());
            alert.setCategoryName(budget.getCategoryName());
            alert.setBudgetAmount(budget.getAmount());
            alert.setSpentAmount(budget.getSpent());
            alert.setUsagePercent(usage);
            alert.setSeverity(usage >= 100 ? "danger" : usage >= 90 ? "warning" : "notice");
            alert.setMessage(budget.getCategoryName() + " budget is " + String.format("%.0f", usage) + "% used");
            alerts.add(alert);
        }
        return alerts;
    }

    private MoneyPlanSummaryDTO.CashflowForecastDTO buildForecast(int days) {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);
        BigDecimal currentBalance = incomeService.getTotalIncomeForCurrentUser().subtract(expenseService.getTotalExpenseForCurrentUser());
        BigDecimal projectedIncome = BigDecimal.ZERO;
        BigDecimal projectedExpense = BigDecimal.ZERO;

        for (RecurringTransactionEntity item : recurringTransactionRepository.findByProfileIdOrderByNextRunDateAsc(profile.getId())) {
            if (item.getNextRunDate() == null || item.getNextRunDate().isBefore(today) || item.getNextRunDate().isAfter(end) || Boolean.FALSE.equals(item.getActive())) continue;
            if ("income".equalsIgnoreCase(item.getType())) projectedIncome = projectedIncome.add(item.getAmount());
            else projectedExpense = projectedExpense.add(item.getAmount());
        }
        for (BillReminderEntity bill : billReminderRepository.findByProfileIdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(profile.getId(), today, end)) {
            projectedExpense = projectedExpense.add(bill.getAmount());
        }

        MoneyPlanSummaryDTO.CashflowForecastDTO forecast = new MoneyPlanSummaryDTO.CashflowForecastDTO();
        forecast.setCurrentBalance(currentBalance);
        forecast.setProjectedIncome(projectedIncome);
        forecast.setProjectedExpense(projectedExpense);
        forecast.setProjectedBalance(currentBalance.add(projectedIncome).subtract(projectedExpense));
        forecast.setStartDate(today);
        forecast.setEndDate(end);
        return forecast;
    }
}
