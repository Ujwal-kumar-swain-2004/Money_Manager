package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.BudgetDTO;
import in.bushansirgur.moneymanager.entity.BudgetEntity;
import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.BudgetRepository;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {
    @Autowired private BudgetRepository budgetRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ProfileService profileService;
    @Autowired private CacheInvalidationService cacheInvalidationService;

    public BudgetDTO save(BudgetDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        BudgetEntity entity = dto.getId() == null ? new BudgetEntity() :
                budgetRepository.findByIdAndProfileId(dto.getId(), profile.getId()).orElseThrow(() -> new RuntimeException("Budget not found"));
        entity.setCategory(category);
        entity.setProfile(profile);
        entity.setMonth(dto.getMonth());
        entity.setYear(dto.getYear());
        entity.setAmount(dto.getAmount());
        BudgetDTO saved = toDTO(budgetRepository.save(entity));
        cacheInvalidationService.clearMoneyCaches();
        return saved;
    }

    public List<BudgetDTO> list(Integer month, Integer year) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return budgetRepository.findByProfileIdAndMonthAndYear(profile.getId(), month, year).stream().map(this::toDTO).toList();
    }

    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        BudgetEntity entity = budgetRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(entity);
        cacheInvalidationService.clearMoneyCaches();
    }

    private BudgetDTO toDTO(BudgetEntity entity) {
        LocalDate start = LocalDate.of(entity.getYear(), entity.getMonth(), 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        BigDecimal spent = expenseRepository.findByProfileIdAndDateBetween(entity.getProfile().getId(), start, end).stream()
                .filter(expense -> expense.getCategory() != null && expense.getCategory().getId().equals(entity.getCategory().getId()))
                .map(ExpenseEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amount = entity.getAmount() == null ? BigDecimal.ZERO : entity.getAmount();
        BudgetDTO dto = new BudgetDTO();
        dto.setId(entity.getId());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());
        dto.setCategoryIcon(entity.getCategory().getIcon());
        dto.setMonth(entity.getMonth());
        dto.setYear(entity.getYear());
        dto.setAmount(amount);
        dto.setSpent(spent);
        dto.setRemaining(amount.subtract(spent));
        dto.setUsagePercent(amount.compareTo(BigDecimal.ZERO) == 0 ? 0 : spent.multiply(BigDecimal.valueOf(100)).divide(amount, 2, RoundingMode.HALF_UP).doubleValue());
        return dto;
    }
}
