package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.FamilyDTO;
import in.bushansirgur.moneymanager.dto.FamilyMemberDTO;
import in.bushansirgur.moneymanager.dto.FamilyTransferDTO;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.FamilyEntity;
import in.bushansirgur.moneymanager.entity.FamilyMemberEntity;
import in.bushansirgur.moneymanager.entity.FamilyTransferEntity;
import in.bushansirgur.moneymanager.entity.IncomeEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import in.bushansirgur.moneymanager.repository.FamilyMemberRepository;
import in.bushansirgur.moneymanager.repository.FamilyRepository;
import in.bushansirgur.moneymanager.repository.FamilyTransferRepository;
import in.bushansirgur.moneymanager.repository.IncomeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional
public class FamilyService {
    @Autowired
    private FamilyRepository familyRepository;
    @Autowired
    private FamilyMemberRepository familyMemberRepository;
    @Autowired
    private FamilyTransferRepository familyTransferRepository;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private IncomeRepository incomeRepository;
    @Autowired
    private ProfileService profileService;

    public List<FamilyDTO> getFamilies() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return familyRepository.findByOwnerId(profile.getId()).stream()
                .sorted(Comparator.comparing(FamilyEntity::getId).reversed())
                .map(this::toDTO)
                .toList();
    }

    public FamilyDTO createFamily(FamilyDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FamilyEntity family = new FamilyEntity();
        family.setName(dto.getName() == null || dto.getName().isBlank() ? "My Family" : dto.getName().trim());
        family.setInviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        family.setOwner(profile);
        FamilyEntity savedFamily = familyRepository.save(family);

        FamilyMemberEntity ownerMember = new FamilyMemberEntity();
        ownerMember.setFamily(savedFamily);
        ownerMember.setProfile(profile);
        ownerMember.setName(profile.getFullName() == null ? "Parent" : profile.getFullName());
        ownerMember.setRole("Parent");
        ownerMember.setAvatar("parent");
        ownerMember.setMonthlyAllowance(BigDecimal.ZERO);
        familyMemberRepository.save(ownerMember);

        return getDashboard(savedFamily.getId());
    }

    public FamilyDTO getDashboard(Long familyId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FamilyEntity family = getOwnedFamily(familyId, profile.getId());
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), start, end)
                .stream()
                .filter(expense -> isFamilyMember(expense.getFamilyMember(), familyId))
                .toList();
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), start, end)
                .stream()
                .filter(income -> isFamilyMember(income.getFamilyMember(), familyId))
                .toList();

        BigDecimal totalExpense = expenses.stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalIncome = incomes.stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        FamilyDTO dto = toDTO(family);
        dto.setMembers(getMemberSummaries(familyId, expenses));
        dto.setTransfers(getTransfers(familyId));
        dto.setTotalIncome(totalIncome);
        dto.setTotalExpense(totalExpense);
        dto.setTotalBalance(totalIncome.subtract(totalExpense));
        return dto;
    }

    public List<FamilyMemberDTO> getMembers(Long familyId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        getOwnedFamily(familyId, profile.getId());
        return getMemberSummaries(familyId, List.of());
    }

    public FamilyMemberDTO addMember(Long familyId, FamilyMemberDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FamilyEntity family = getOwnedFamily(familyId, profile.getId());
        FamilyMemberEntity member = new FamilyMemberEntity();
        member.setFamily(family);
        member.setName(dto.getName());
        member.setRole(dto.getRole() == null || dto.getRole().isBlank() ? "Member" : dto.getRole());
        member.setAvatar(dto.getAvatar());
        member.setMonthlyAllowance(defaultAmount(dto.getMonthlyAllowance()));
        return toMemberDTO(familyMemberRepository.save(member), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public List<FamilyTransferDTO> getTransfers(Long familyId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        getOwnedFamily(familyId, profile.getId());
        return familyTransferRepository.findByFamilyIdOrderByTransferDateDesc(familyId)
                .stream()
                .map(this::toTransferDTO)
                .toList();
    }

    public FamilyTransferDTO addTransfer(Long familyId, FamilyTransferDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FamilyEntity family = getOwnedFamily(familyId, profile.getId());
        FamilyTransferEntity transfer = new FamilyTransferEntity();
        transfer.setFamily(family);
        transfer.setAmount(defaultAmount(dto.getAmount()));
        transfer.setTransferDate(dto.getTransferDate());
        transfer.setNote(dto.getNote());
        if (dto.getFromMemberId() != null) {
            transfer.setFromMember(getFamilyMember(dto.getFromMemberId(), familyId));
        }
        if (dto.getToMemberId() != null) {
            transfer.setToMember(getFamilyMember(dto.getToMemberId(), familyId));
        }
        return toTransferDTO(familyTransferRepository.save(transfer));
    }

    private List<FamilyMemberDTO> getMemberSummaries(Long familyId, List<ExpenseEntity> monthExpenses) {
        List<FamilyTransferEntity> transfers = familyTransferRepository.findByFamilyIdOrderByTransferDateDesc(familyId);
        return familyMemberRepository.findByFamilyIdOrderByIdAsc(familyId).stream()
                .map(member -> {
                    BigDecimal spent = monthExpenses.stream()
                            .filter(expense -> isSameMember(expense.getFamilyMember(), member.getId()))
                            .map(ExpenseEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal received = transfers.stream()
                            .filter(transfer -> isSameMember(transfer.getToMember(), member.getId()))
                            .map(FamilyTransferEntity::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return toMemberDTO(member, spent, received);
                })
                .toList();
    }

    private FamilyEntity getOwnedFamily(Long familyId, Long profileId) {
        return familyRepository.findByIdAndOwnerId(familyId, profileId)
                .orElseThrow(() -> new RuntimeException("Family not found"));
    }

    private FamilyMemberEntity getFamilyMember(Long memberId, Long familyId) {
        return familyMemberRepository.findByIdAndFamilyId(memberId, familyId)
                .orElseThrow(() -> new RuntimeException("Family member not found"));
    }

    private boolean isFamilyMember(FamilyMemberEntity member, Long familyId) {
        return member != null && member.getFamily() != null && member.getFamily().getId().equals(familyId);
    }

    private boolean isSameMember(FamilyMemberEntity member, Long memberId) {
        return member != null && member.getId().equals(memberId);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private FamilyDTO toDTO(FamilyEntity entity) {
        FamilyDTO dto = new FamilyDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setInviteCode(entity.getInviteCode());
        return dto;
    }

    private FamilyMemberDTO toMemberDTO(FamilyMemberEntity entity, BigDecimal spent, BigDecimal received) {
        BigDecimal allowance = defaultAmount(entity.getMonthlyAllowance());
        FamilyMemberDTO dto = new FamilyMemberDTO();
        dto.setId(entity.getId());
        dto.setFamilyId(entity.getFamily().getId());
        dto.setName(entity.getName());
        dto.setRole(entity.getRole());
        dto.setAvatar(entity.getAvatar());
        dto.setMonthlyAllowance(allowance);
        dto.setMonthSpent(spent);
        dto.setMonthReceived(received);
        dto.setAllowanceRemaining(allowance.add(received).subtract(spent));
        return dto;
    }

    private FamilyTransferDTO toTransferDTO(FamilyTransferEntity entity) {
        FamilyTransferDTO dto = new FamilyTransferDTO();
        dto.setId(entity.getId());
        dto.setFamilyId(entity.getFamily().getId());
        dto.setAmount(entity.getAmount());
        dto.setTransferDate(entity.getTransferDate());
        dto.setNote(entity.getNote());
        if (entity.getFromMember() != null) {
            dto.setFromMemberId(entity.getFromMember().getId());
            dto.setFromMemberName(entity.getFromMember().getName());
        }
        if (entity.getToMember() != null) {
            dto.setToMemberId(entity.getToMember().getId());
            dto.setToMemberName(entity.getToMember().getName());
        }
        return dto;
    }
}
