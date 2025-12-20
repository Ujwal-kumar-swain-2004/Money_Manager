package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.AnalyticsDTO;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ProfileService profileService;

    public AnalyticsDTO getMonthlyAnalytics(Integer month, Integer year) {
        ProfileEntity profile = profileService.getCurrentProfile();
        YearMonth current = YearMonth.of(year, month);
        YearMonth previous = current.minusMonths(1);
        List<ExpenseEntity> currentExpenses = expensesFor(profile.getId(), current);
        List<ExpenseEntity> previousExpenses = expensesFor(profile.getId(), previous);
        BigDecimal currentTotal = total(currentExpenses);
        BigDecimal previousTotal = total(previousExpenses);

        AnalyticsDTO dto = new AnalyticsDTO();
        dto.setCurrentMonthExpense(currentTotal);
        dto.setPreviousMonthExpense(previousTotal);
        dto.setMonthChangePercent(changePercent(currentTotal, previousTotal));
        dto.setCategoryBreakdown(categoryBreakdown(currentExpenses, currentTotal));
        dto.setCalendarSpend(calendarSpend(currentExpenses));
        return dto;
    }

    private List<ExpenseEntity> expensesFor(Long profileId, YearMonth month) {
        return expenseRepository.findByProfileIdAndDateBetween(profileId, month.atDay(1), month.atEndOfMonth());
    }

    private BigDecimal total(List<ExpenseEntity> expenses) {
        return expenses.stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal changePercent(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        return current.subtract(previous).multiply(BigDecimal.valueOf(100)).divide(previous, 2, RoundingMode.HALF_UP);
    }

    private List<AnalyticsDTO.CategoryBreakdownDTO> categoryBreakdown(List<ExpenseEntity> expenses, BigDecimal total) {
        Map<Long, List<ExpenseEntity>> grouped = expenses.stream().filter(e -> e.getCategory() != null).collect(Collectors.groupingBy(e -> e.getCategory().getId()));
        return grouped.values().stream().map(items -> {
            ExpenseEntity first = items.get(0);
            BigDecimal amount = total(items);
            AnalyticsDTO.CategoryBreakdownDTO dto = new AnalyticsDTO.CategoryBreakdownDTO();
            dto.setCategoryId(first.getCategory().getId());
            dto.setCategoryName(first.getCategory().getName());
            dto.setIcon(first.getCategory().getIcon());
            dto.setAmount(amount);
            dto.setPercent(total.compareTo(BigDecimal.ZERO) == 0 ? 0 : amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP).doubleValue());
            return dto;
        }).sorted(Comparator.comparing(AnalyticsDTO.CategoryBreakdownDTO::getAmount).reversed()).toList();
    }

    private List<AnalyticsDTO.CalendarSpendDTO> calendarSpend(List<ExpenseEntity> expenses) {
        Map<LocalDate, BigDecimal> grouped = expenses.stream().collect(Collectors.groupingBy(ExpenseEntity::getDate, Collectors.reducing(BigDecimal.ZERO, ExpenseEntity::getAmount, BigDecimal::add)));
        return grouped.entrySet().stream().map(entry -> {
            AnalyticsDTO.CalendarSpendDTO dto = new AnalyticsDTO.CalendarSpendDTO();
            dto.setDate(entry.getKey());
            dto.setAmount(entry.getValue());
            return dto;
        }).sorted(Comparator.comparing(AnalyticsDTO.CalendarSpendDTO::getDate)).toList();
    }
}
