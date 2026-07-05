package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.AiChatMessageDTO;
import in.bushansirgur.moneymanager.entity.AiChatMessageEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.AiChatMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AiConversationMemoryService {

    private static final int MAX_MESSAGES_FOR_PROMPT = 16;

    @Autowired
    private AiChatMessageRepository aiChatMessageRepository;

    @Autowired
    private ProfileService profileService;

    public String recentConversationForProfile(Long profileId) {
        List<AiChatMessageEntity> messages = recentMessages(profileId);
        if (messages.isEmpty()) {
            return "No previous conversation in this session.";
        }

        StringBuilder sb = new StringBuilder();
        for (AiChatMessageEntity message : messages) {
            sb.append("user".equalsIgnoreCase(message.getRole()) ? "User: " : "Assistant: ");
            sb.append(message.getMessage()).append("\n");
        }
        return sb.toString().trim();
    }

    @Transactional
    public void remember(Long profileId, String userQuestion, String assistantAnswer) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (!profile.getId().equals(profileId)) {
            throw new RuntimeException("Cannot store AI memory for another profile");
        }
        saveMessage(profile, "user", trim(userQuestion));
        saveMessage(profile, "assistant", trim(assistantAnswer));
    }

    public List<AiChatMessageDTO> historyForCurrentProfile() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<AiChatMessageEntity> messages = aiChatMessageRepository.findTop50ByProfileIdOrderByCreatedAtDesc(profile.getId());
        Collections.reverse(messages);
        return messages.stream().map(this::toDTO).toList();
    }

    @Transactional
    public void clear(Long profileId) {
        aiChatMessageRepository.deleteByProfileId(profileId);
    }

    private List<AiChatMessageEntity> recentMessages(Long profileId) {
        List<AiChatMessageEntity> messages = aiChatMessageRepository.findTop20ByProfileIdOrderByCreatedAtDesc(profileId);
        Collections.reverse(messages);
        if (messages.size() <= MAX_MESSAGES_FOR_PROMPT) {
            return messages;
        }
        return messages.subList(messages.size() - MAX_MESSAGES_FOR_PROMPT, messages.size());
    }

    private void saveMessage(ProfileEntity profile, String role, String message) {
        AiChatMessageEntity entity = new AiChatMessageEntity();
        entity.setProfile(profile);
        entity.setRole(role);
        entity.setMessage(message);
        aiChatMessageRepository.save(entity);
    }

    private AiChatMessageDTO toDTO(AiChatMessageEntity entity) {
        AiChatMessageDTO dto = new AiChatMessageDTO();
        dto.setId(entity.getId());
        dto.setRole(entity.getRole());
        dto.setText(entity.getMessage());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }
}
