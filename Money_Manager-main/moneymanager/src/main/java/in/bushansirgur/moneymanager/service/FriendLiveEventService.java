package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.FriendLiveEventDTO;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class FriendLiveEventService {
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired private PlanLimitService planLimitService;

    public void publish(ProfileEntity profile, String type, String message) {
        if (!planLimitService.isPro(profile)) return;
        messagingTemplate.convertAndSend(
                "/topic/friends/" + profile.getId(),
                new FriendLiveEventDTO(type, message, profile.getId())
        );
    }
}
