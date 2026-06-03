package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.ExpenseDTO;
import in.bushansirgur.moneymanager.dto.IncomeDTO;
import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.FamilyMemberEntity;
import in.bushansirgur.moneymanager.entity.IncomeEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import in.bushansirgur.moneymanager.repository.FamilyMemberRepository;
import in.bushansirgur.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    @Autowired
    private  CategoryRepository categoryRepository;
    @Autowired
    private  IncomeRepository incomeRepository;
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    @Autowired
    private  ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        IncomeEntity newExpense = toEntity(dto, profile, category);
        newExpense = incomeRepository.save(newExpense);
        return toDTO(newExpense);
    }

    // Retrieves all incomes for current month/based on the start date and end date
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    //delete income by id for current user
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorized to delete this income");
        }
        incomeRepository.delete(entity);
    }

    // Get latest 5 incomes for current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Get total incomes for current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total: BigDecimal.ZERO;
    }

    //filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    // Fetch incomes for a specific user by profileId and date range (used by scheduled jobs)
    public List<IncomeDTO> getIncomesForUserInRange(Long profileId, LocalDate startDate, LocalDate endDate) {
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(profileId, startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    //helper methods
    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        IncomeEntity entity = new IncomeEntity();
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

    private IncomeDTO toDTO(IncomeEntity entity) {
        IncomeDTO dto = new IncomeDTO();
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
