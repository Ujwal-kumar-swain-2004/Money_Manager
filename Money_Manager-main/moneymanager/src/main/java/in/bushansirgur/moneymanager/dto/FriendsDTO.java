package in.bushansirgur.moneymanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class FriendsDTO {
    private List<FriendDTO> friends;
    private List<FriendGroupDTO> groups;
    private List<SharedExpenseDTO> expenses;
    private List<SettlementDTO> settlements;
    private List<ReminderDTO> reminders;
    private List<ActivityDTO> activities;
    private List<RecurringSharedExpenseDTO> recurringExpenses;
    private List<GroupReportDTO> groupReports;
    private List<SettlementSuggestionDTO> settlementSuggestions;
    private Map<String, Object> limits;
    private String planType;
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
    public List<RecurringSharedExpenseDTO> getRecurringExpenses() { return recurringExpenses; }
    public void setRecurringExpenses(List<RecurringSharedExpenseDTO> recurringExpenses) { this.recurringExpenses = recurringExpenses; }
    public List<GroupReportDTO> getGroupReports() { return groupReports; }
    public void setGroupReports(List<GroupReportDTO> groupReports) { this.groupReports = groupReports; }
    public List<SettlementSuggestionDTO> getSettlementSuggestions() { return settlementSuggestions; }
    public void setSettlementSuggestions(List<SettlementSuggestionDTO> settlementSuggestions) { this.settlementSuggestions = settlementSuggestions; }
    public Map<String, Object> getLimits() { return limits; }
    public void setLimits(Map<String, Object> limits) { this.limits = limits; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
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
        public String inviteLink;
        public BigDecimal balance;
    }

    public static class FriendGroupDTO {
        public Long id;
        public String name;
        public String type;
        public String icon;
        public List<Long> friendIds;
        public List<GroupMemberDTO> members;
    }

    public static class GroupMemberDTO {
        public Long friendId;
        public String friendName;
        public String role;
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
        public String receiptUrl;
        public Long paidByFriendId;
        public String paidByFriendName;
        public Long groupId;
        public String groupName;
        public List<SplitDTO> splits;
        public List<CommentDTO> comments;
    }

    public static class RecurringSharedExpenseDTO {
        public Long id;
        public String title;
        public BigDecimal amount;
        public String category;
        public String splitType;
        public String frequency;
        public LocalDate nextDueDate;
        public Boolean active;
        public String note;
        public String receiptUrl;
        public Long paidByFriendId;
        public String paidByFriendName;
        public Long groupId;
        public String groupName;
    }

    public static class GroupReportDTO {
        public Long groupId;
        public String groupName;
        public BigDecimal totalSpent;
        public BigDecimal settlementTotal;
        public BigDecimal pendingBalance;
        public Long expenseCount;
        public Long memberCount;
        public String topCategory;
        public String topPayerName;
    }

    public static class SettlementSuggestionDTO {
        public String fromName;
        public String toName;
        public Long friendId;
        public BigDecimal amount;
        public String note;
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
