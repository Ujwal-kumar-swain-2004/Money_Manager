package in.bushansirgur.moneymanager.dto;

import java.time.LocalDateTime;

public class FriendLiveEventDTO {
    private String type;
    private String message;
    private Long profileId;
    private LocalDateTime createdAt;

    public FriendLiveEventDTO(String type, String message, Long profileId) {
        this.type = type;
        this.message = message;
        this.profileId = profileId;
        this.createdAt = LocalDateTime.now();
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
