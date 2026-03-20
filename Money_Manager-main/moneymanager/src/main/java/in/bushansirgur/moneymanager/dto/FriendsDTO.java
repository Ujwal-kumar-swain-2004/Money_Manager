package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FriendsDTO {
    private List<FriendDTO> friends;
    private List<FriendGroupDTO> groups;
    private List<SharedExpenseDTO> expenses;
    private List<SettlementDTO> settlements;
    private List<ReminderDTO> reminders;
    private List<ActivityDTO> activities;
    private BigDecimal totalYouOwe;
    private BigDecimal totalOwedToYou;
    private BigDecimal netBalance;

    public List<FriendDTO> getFriends() { return friends; }
    public void setFriends(List<FriendDTO> friends) { this.friends = friends; }
    public List<FriendGroupDTO> getGroups() { return groups; }
    public void setGroups(List<FriendGroupDTO> groups) { this.groups = groups; }
    public List<SharedExpenseDTO> getExpenses() { return expenses; }
    public void setExpenses(List<SharedExpenseDTO> expenses) { this.expenses = expenses; }
    public List<SettlementDTO> getSettlements() { return settlements; }
    public void setSettlements(List<SettlementDTO> settlements) { this.settlements = settlements; }
    public List<ReminderDTO> getReminders() { return reminders; }
    public void setReminders(List<ReminderDTO> reminders) { this.reminders = reminders; }
    public List<ActivityDTO> getActivities() { return activities; }
    public void setActivities(List<ActivityDTO> activities) { this.activities = activities; }
    public BigDecimal getTotalYouOwe() { return totalYouOwe; }
    public void setTotalYouOwe(BigDecimal totalYouOwe) { this.totalYouOwe = totalYouOwe; }
    public BigDecimal getTotalOwedToYou() { return totalOwedToYou; }
    public void setTotalOwedToYou(BigDecimal totalOwedToYou) { this.totalOwedToYou = totalOwedToYou; }
    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }

    public static class FriendDTO {
        public Long id;
        public String name;
        public String email;
        public String phone;
        public String avatarUrl;
        public String upiId;
        public String status;
        public String inviteCode;
        public BigDecimal balance;
    }

    public static class FriendGroupDTO {
        public Long id;
        public String name;
        public String type;
        public String icon;
        public List<Long> friendIds;
    }

    public static class SplitDTO {
        public Long friendId;
        public String friendName;
        public BigDecimal amount;
        public BigDecimal percentValue;
        public BigDecimal shares;
    }

    public static class SharedExpenseDTO {
        public Long id;
        public String title;
        public BigDecimal amount;
        public LocalDate expenseDate;
        public String category;
        public String splitType;
        public String note;
        public Long paidByFriendId;
        public String paidByFriendName;
        public Long groupId;
        public String groupName;
        public List<SplitDTO> splits;
        public List<CommentDTO> comments;
    }

    public static class SettlementDTO {
        public Long id;
        public Long friendId;
        public String friendName;
        public BigDecimal amount;
        public LocalDate settlementDate;
        public String method;
        public String direction;
        public String note;
    }

    public static class ReminderDTO {
        public Long id;
        public Long friendId;
        public String friendName;
        public String message;
        public BigDecimal amount;
        public LocalDate dueDate;
        public Boolean sent;
    }

    public static class CommentDTO {
        public Long id;
        public Long expenseId;
        public String comment;
        public LocalDateTime createdAt;
    }

    public static class ActivityDTO {
        public Long id;
        public String type;
        public String message;
        public LocalDateTime createdAt;
    }
}
