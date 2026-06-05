package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.ExpenseDTO;
import in.bushansirgur.moneymanager.dto.IncomeDTO;
import in.bushansirgur.moneymanager.dto.RecentTransactionDTO;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.config.RedisCacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

@Service
public class DashboardService {
    @Autowired
    private  IncomeService incomeService;
    @Autowired
    private  ExpenseService expenseService;
    @Autowired
    private  ProfileService profileService;

    @Cacheable(value = RedisCacheConfig.DASHBOARD_CACHE, key = "@profileService.getCurrentProfileId()")
    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> recentTransactions = concat(
                latestIncomes.stream().map(income -> {
                    RecentTransactionDTO dto = new RecentTransactionDTO();
                    dto.setId(income.getId());
                    dto.setProfileId(profile.getId());
                    dto.setIcon(income.getIcon());
                    dto.setName(income.getName());
                    dto.setAmount(income.getAmount());
                    dto.setDate(income.getDate());
                    dto.setCreatedAt(income.getCreatedAt());
                    dto.setUpdatedAt(income.getUpdatedAt());
                    dto.setType("income");
                    return dto;
                }),
                latestExpenses.stream().map(expense -> {
                    RecentTransactionDTO dto = new RecentTransactionDTO();
                    dto.setId(expense.getId());
                    dto.setProfileId(profile.getId());
                    dto.setIcon(expense.getIcon());
                    dto.setName(expense.getName());
                    dto.setAmount(expense.getAmount());
                    dto.setDate(expense.getDate());
                    dto.setCreatedAt(expense.getCreatedAt());
                    dto.setUpdatedAt(expense.getUpdatedAt());
                    dto.setType("expense");
                    return dto;
                })
        ).sorted((a, b) -> {
            int cmp = b.getDate().compareTo(a.getDate());
            if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            return cmp;
        }).collect(Collectors.toList());

        returnValue.put("totalBalance",
                incomeService.getTotalIncomeForCurrentUser()
                        .subtract(expenseService.getTotalExpenseForCurrentUser()));
        returnValue.put("totalIncome", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("totalExpense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("recent5Expenses", latestExpenses);
        returnValue.put("recent5Incomes", latestIncomes);
        returnValue.put("recentTransactions", recentTransactions);
        return returnValue;
    }

}
