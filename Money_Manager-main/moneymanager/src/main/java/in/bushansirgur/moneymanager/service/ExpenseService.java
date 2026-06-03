package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.ExpenseDTO;
import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.FamilyMemberEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import in.bushansirgur.moneymanager.repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    @Autowired
    private  CategoryRepository categoryRepository;
    @Autowired
    private  ExpenseRepository expenseRepository;
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    @Autowired
    private  ProfileService profileService;
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        ExpenseEntity newExpense = toEntity(dto, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }
        expenseRepository.delete(entity);
    }
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total: BigDecimal.ZERO;
    }
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    //Notifications — get expenses for a specific user on a specific date
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDTO).toList();
    }

    // Fetch expenses for a specific user by profileId and date range (used by scheduled jobs)
    public List<ExpenseDTO> getExpensesForUserInRange(Long profileId, LocalDate startDate, LocalDate endDate) {
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDateBetween(profileId, startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }
    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category) {
        ExpenseEntity entity = new ExpenseEntity();
        entity.setName(dto.getName());
        entity.setIcon(dto.getIcon());
        entity.setAmount(dto.getAmount());
        entity.setDate(dto.getDate());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setNotes(dto.getNotes());
        entity.setTags(dto.getTags());
        entity.setReceiptUrl(dto.getReceiptUrl());
        if (dto.getFamilyMemberId() != null) {
            FamilyMemberEntity familyMember = familyMemberRepository.findById(dto.getFamilyMemberId())
                    .orElseThrow(() -> new RuntimeException("Family member not found"));
            if (!familyMember.getFamily().getOwner().getId().equals(profile.getId())) {
                throw new RuntimeException("Unauthorized family member");
            }
            entity.setFamilyMember(familyMember);
        }
        entity.setProfile(profile);
        entity.setCategory(category);
        return entity;
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setCategoryId(entity.getCategory() != null ? entity.getCategory().getId() : null);
        dto.setCategoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A");
        dto.setAmount(entity.getAmount());
        dto.setDate(entity.getDate());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setNotes(entity.getNotes());
        dto.setTags(entity.getTags());
        dto.setReceiptUrl(entity.getReceiptUrl());
        if (entity.getFamilyMember() != null) {
            dto.setFamilyMemberId(entity.getFamilyMember().getId());
            dto.setFamilyMemberName(entity.getFamilyMember().getName());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }


}
