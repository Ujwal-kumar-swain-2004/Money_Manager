package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.SavingsGoalDTO;
import in.bushansirgur.moneymanager.dto.SavingsContributionDTO;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.entity.SavingsContributionEntity;
import in.bushansirgur.moneymanager.entity.SavingsGoalEntity;
import in.bushansirgur.moneymanager.repository.SavingsContributionRepository;
import in.bushansirgur.moneymanager.repository.SavingsGoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class SavingsGoalService {
    @Autowired private SavingsGoalRepository savingsGoalRepository;
    @Autowired private SavingsContributionRepository savingsContributionRepository;
    @Autowired private ProfileService profileService;
    @Autowired private CacheInvalidationService cacheInvalidationService;

    public SavingsGoalDTO save(SavingsGoalDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity entity = dto.getId() == null ? new SavingsGoalEntity() :
                savingsGoalRepository.findByIdAndProfileId(dto.getId(), profile.getId()).orElseThrow(() -> new RuntimeException("Goal not found"));
        entity.setName(dto.getName());
        entity.setIcon(dto.getIcon());
        entity.setTargetAmount(dto.getTargetAmount());
        entity.setSavedAmount(dto.getSavedAmount());
        entity.setTargetDate(dto.getTargetDate());
        entity.setProfile(profile);
        SavingsGoalDTO saved = toDTO(savingsGoalRepository.save(entity));
        cacheInvalidationService.clearMoneyCaches();
        return saved;
    }

    public List<SavingsGoalDTO> list() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return savingsGoalRepository.findByProfileIdOrderByTargetDateAsc(profile.getId()).stream().map(this::toDTO).toList();
    }

    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity entity = savingsGoalRepository.findByIdAndProfileId(id, profile.getId()).orElseThrow(() -> new RuntimeException("Goal not found"));
        savingsGoalRepository.delete(entity);
        cacheInvalidationService.clearMoneyCaches();
    }

    public SavingsContributionDTO addContribution(Long goalId, SavingsContributionDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity goal = savingsGoalRepository.findByIdAndProfileId(goalId, profile.getId()).orElseThrow(() -> new RuntimeException("Goal not found"));
        SavingsContributionEntity contribution = new SavingsContributionEntity();
        contribution.setGoal(goal);
        contribution.setProfile(profile);
        contribution.setAmount(dto.getAmount());
        contribution.setContributionDate(dto.getContributionDate());
        contribution.setNote(dto.getNote());
        contribution = savingsContributionRepository.save(contribution);
        BigDecimal savedAmount = goal.getSavedAmount() == null ? BigDecimal.ZERO : goal.getSavedAmount();
        goal.setSavedAmount(savedAmount.add(dto.getAmount()));
        savingsGoalRepository.save(goal);
        cacheInvalidationService.clearMoneyCaches();
        return toContributionDTO(contribution);
    }

    public List<SavingsContributionDTO> listContributions(Long goalId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return savingsContributionRepository.findByGoalIdAndProfileIdOrderByContributionDateDesc(goalId, profile.getId()).stream().map(this::toContributionDTO).toList();
    }

    private SavingsGoalDTO toDTO(SavingsGoalEntity entity) {
        BigDecimal target = entity.getTargetAmount() == null ? BigDecimal.ZERO : entity.getTargetAmount();
        BigDecimal saved = entity.getSavedAmount() == null ? BigDecimal.ZERO : entity.getSavedAmount();
        SavingsGoalDTO dto = new SavingsGoalDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setIcon(entity.getIcon());
        dto.setTargetAmount(target);
        dto.setSavedAmount(saved);
        dto.setTargetDate(entity.getTargetDate());
        dto.setProgressPercent(target.compareTo(BigDecimal.ZERO) == 0 ? 0 : saved.multiply(BigDecimal.valueOf(100)).divide(target, 2, RoundingMode.HALF_UP).doubleValue());
        return dto;
    }

    private SavingsContributionDTO toContributionDTO(SavingsContributionEntity entity) {
        SavingsContributionDTO dto = new SavingsContributionDTO();
        dto.setId(entity.getId());
        dto.setGoalId(entity.getGoal().getId());
        dto.setAmount(entity.getAmount());
        dto.setContributionDate(entity.getContributionDate());
        dto.setNote(entity.getNote());
        return dto;
    }
}
