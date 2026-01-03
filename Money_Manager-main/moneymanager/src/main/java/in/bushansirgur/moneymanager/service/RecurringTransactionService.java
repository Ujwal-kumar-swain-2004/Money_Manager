package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.RecurringTransactionDTO;
import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.IncomeEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.entity.RecurringTransactionEntity;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import in.bushansirgur.moneymanager.repository.IncomeRepository;
import in.bushansirgur.moneymanager.repository.RecurringTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringTransactionService {
    @Autowired private RecurringTransactionRepository recurringTransactionRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private IncomeRepository incomeRepository;
    @Autowired private ExpenseRepository expenseRepository;
    @Autowired private ProfileService profileService;

    public RecurringTransactionDTO save(RecurringTransactionDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId()).orElseThrow(() -> new RuntimeException("Category not found"));
        RecurringTransactionEntity entity = dto.getId() == null ? new RecurringTransactionEntity() :
                recurringTransactionRepository.findByIdAndProfileId(dto.getId(), profile.getId()).orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        entity.setName(dto.getName());
        entity.setIcon(dto.getIcon());
        entity.setType(dto.getType());
        entity.setFrequency(dto.getFrequency());
        entity.setAmount(dto.getAmount());
        entity.setStartDate(dto.getStartDate());
        entity.setNextRunDate(dto.getNextRunDate());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setActive(dto.getActive());
        entity.setCategory(category);
        entity.setProfile(profile);
        return toDTO(recurringTransactionRepository.save(entity));
    }

    public List<RecurringTransactionDTO> list() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return recurringTransactionRepository.findByProfileIdOrderByNextRunDateAsc(profile.getId()).stream().map(this::toDTO).toList();
    }

    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        RecurringTransactionEntity entity = recurringTransactionRepository.findByIdAndProfileId(id, profile.getId()).orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
        recurringTransactionRepository.delete(entity);
    }

    public List<RecurringTransactionDTO> processDue() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate today = LocalDate.now();
        List<RecurringTransactionEntity> dueItems = recurringTransactionRepository.findByProfileIdAndActiveTrueAndNextRunDateLessThanEqual(profile.getId(), today);
        dueItems.forEach(item -> {
            if ("income".equalsIgnoreCase(item.getType())) {
                IncomeEntity income = new IncomeEntity();
                income.setName(item.getName());
                income.setIcon(item.getIcon());
                income.setAmount(item.getAmount());
                income.setDate(item.getNextRunDate());
                income.setPaymentMethod(item.getPaymentMethod());
                income.setNotes("Created from recurring rule");
                income.setCategory(item.getCategory());
                income.setProfile(profile);
                incomeRepository.save(income);
            } else {
                ExpenseEntity expense = new ExpenseEntity();
                expense.setName(item.getName());
                expense.setIcon(item.getIcon());
                expense.setAmount(item.getAmount());
                expense.setDate(item.getNextRunDate());
                expense.setPaymentMethod(item.getPaymentMethod());
                expense.setNotes("Created from recurring rule");
                expense.setCategory(item.getCategory());
                expense.setProfile(profile);
                expenseRepository.save(expense);
            }
            item.setNextRunDate(nextDate(item.getNextRunDate(), item.getFrequency()));
            recurringTransactionRepository.save(item);
        });
        return dueItems.stream().map(this::toDTO).toList();
    }

    public LocalDate nextDate(LocalDate date, String frequency) {
        if (date == null) return LocalDate.now();
        if ("weekly".equalsIgnoreCase(frequency)) return date.plusWeeks(1);
        if ("yearly".equalsIgnoreCase(frequency)) return date.plusYears(1);
        return date.plusMonths(1);
    }

    private RecurringTransactionDTO toDTO(RecurringTransactionEntity entity) {
        RecurringTransactionDTO dto = new RecurringTransactionDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setType(entity.getType());
        dto.setFrequency(entity.getFrequency());
        dto.setAmount(entity.getAmount());
        dto.setStartDate(entity.getStartDate());
        dto.setNextRunDate(entity.getNextRunDate());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setActive(entity.getActive());
        dto.setCategoryId(entity.getCategory().getId());
        dto.setCategoryName(entity.getCategory().getName());
        return dto;
    }
}
