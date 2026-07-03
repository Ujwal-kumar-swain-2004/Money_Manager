package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.BillReminderDTO;
import in.bushansirgur.moneymanager.entity.BillReminderEntity;
import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.BillReminderRepository;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BillReminderService {
    @Autowired private BillReminderRepository billReminderRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProfileService profileService;
    @Autowired private CacheInvalidationService cacheInvalidationService;

    public BillReminderDTO save(BillReminderDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        BillReminderEntity entity = dto.getId() == null ? new BillReminderEntity() :
                billReminderRepository.findByIdAndProfileId(dto.getId(), profile.getId()).orElseThrow(() -> new RuntimeException("Reminder not found"));
        CategoryEntity category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId()).orElseThrow(() -> new RuntimeException("Category not found"));
        }
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setAmount(dto.getAmount());
        entity.setDueDate(dto.getDueDate());
        entity.setFrequency(dto.getFrequency());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setPaid(dto.getPaid());
        entity.setCategory(category);
        entity.setProfile(profile);
        BillReminderDTO saved = toDTO(billReminderRepository.save(entity));
        cacheInvalidationService.clearMoneyCaches();
        return saved;
    }

    public List<BillReminderDTO> list() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return billReminderRepository.findByProfileIdOrderByDueDateAsc(profile.getId()).stream().map(this::toDTO).toList();
    }

    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        BillReminderEntity entity = billReminderRepository.findByIdAndProfileId(id, profile.getId()).orElseThrow(() -> new RuntimeException("Reminder not found"));
        billReminderRepository.delete(entity);
        cacheInvalidationService.clearMoneyCaches();
    }

    public List<BillReminderDTO> upcoming(int days) {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate today = LocalDate.now();
        return billReminderRepository.findByProfileIdAndPaidFalseAndDueDateBetweenOrderByDueDateAsc(profile.getId(), today, today.plusDays(days)).stream().map(this::toDTO).toList();
    }

    private BillReminderDTO toDTO(BillReminderEntity entity) {
        BillReminderDTO dto = new BillReminderDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setAmount(entity.getAmount());
        dto.setDueDate(entity.getDueDate());
        dto.setFrequency(entity.getFrequency());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setPaid(entity.getPaid());
        dto.setCategoryId(entity.getCategory() != null ? entity.getCategory().getId() : null);
        dto.setCategoryName(entity.getCategory() != null ? entity.getCategory().getName() : null);
        dto.setDaysUntilDue(entity.getDueDate() == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), entity.getDueDate()));
        return dto;
    }
}
