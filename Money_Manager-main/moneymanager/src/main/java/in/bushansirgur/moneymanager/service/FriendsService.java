package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.FriendsDTO;
import in.bushansirgur.moneymanager.entity.*;
import in.bushansirgur.moneymanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendsService {
    @Autowired private ProfileService profileService;
    @Autowired private FriendRepository friendRepository;
    @Autowired private FriendGroupRepository groupRepository;
    @Autowired private FriendGroupMemberRepository groupMemberRepository;
    @Autowired private SharedExpenseRepository expenseRepository;
    @Autowired private SharedExpenseSplitRepository splitRepository;
    @Autowired private FriendSettlementRepository settlementRepository;
    @Autowired private FriendReminderRepository reminderRepository;
    @Autowired private FriendCommentRepository commentRepository;
    @Autowired private FriendActivityRepository activityRepository;
    @Autowired private RecurringSharedExpenseRepository recurringSharedExpenseRepository;
    @Autowired private PlanLimitService planLimitService;
    @Autowired private FriendLiveEventService friendLiveEventService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public FriendsDTO dashboard() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<Long, BigDecimal> balances = calculateBalances(profile.getId());
        List<FriendsDTO.FriendDTO> friends = friendRepository.findByProfileIdOrderByNameAsc(profile.getId()).stream()
                .map(friend -> toFriendDTO(friend, balances.getOrDefault(friend.getId(), BigDecimal.ZERO)))
                .toList();
        FriendsDTO dto = new FriendsDTO();
        dto.setFriends(friends);
        dto.setGroups(getGroups());
        dto.setExpenses(getExpenses());
        dto.setSettlements(getSettlements());
        dto.setReminders(getReminders());
        dto.setActivities(getActivities());
        dto.setRecurringExpenses(getRecurringExpenses());
        dto.setGroupReports(planLimitService.isPro(profile) ? getGroupReports() : List.of());
        dto.setSettlementSuggestions(planLimitService.isPro(profile) ? getSettlementSuggestions() : List.of());
        dto.setPlanType(planLimitService.isPro(profile) ? "PRO" : "FREE");
        dto.setLimits(planLimitService.friendLimits(
                profile,
                friendRepository.countByProfileId(profile.getId()),
                groupRepository.countByProfileId(profile.getId()),
                sharedExpenseCountThisMonth(profile.getId())
        ));
        dto.setTotalOwedToYou(friends.stream().map(friend -> friend.balance.max(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTotalYouOwe(friends.stream().map(friend -> friend.balance.min(BigDecimal.ZERO).abs()).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setNetBalance(dto.getTotalOwedToYou().subtract(dto.getTotalYouOwe()));
        return dto;
    }

    public FriendsDTO.FriendDTO addFriend(FriendsDTO.FriendDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.checkFreeLimit(profile, friendRepository.countByProfileId(profile.getId()), PlanLimitService.FREE_FRIENDS, "friends");
        FriendEntity friend = new FriendEntity();
        friend.setProfile(profile);
        friend.setName(dto.name);
        friend.setEmail(dto.email);
        friend.setPhone(dto.phone);
        friend.setAvatarUrl(dto.avatarUrl);
        friend.setUpiId(dto.upiId);
        friend.setStatus(dto.status == null || dto.status.isBlank() ? "active" : dto.status);
        friend.setOpeningBalance(dto.balance == null ? BigDecimal.ZERO : dto.balance);
        friend = friendRepository.save(friend);
        activity(profile, "friend", "Added friend " + friend.getName());
        return toFriendDTO(friend, friend.getOpeningBalance());
    }

    public FriendsDTO.FriendDTO inviteFriend(FriendsDTO.FriendDTO dto) {
        dto.status = "invited";
        FriendsDTO.FriendDTO friend = addFriend(dto);
        activity(profileService.getCurrentProfile(), "invite", "Invite created for " + friend.name);
        return friend;
    }

    public FriendsDTO.FriendDTO updateFriendStatus(Long friendId, String status) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FriendEntity friend = getFriend(friendId, profile.getId());
        friend.setStatus(status);
        friend = friendRepository.save(friend);
        activity(profile, "friend", friend.getName() + " marked as " + status);
        return toFriendDTO(friend, calculateBalances(profile.getId()).getOrDefault(friend.getId(), BigDecimal.ZERO));
    }

    public FriendsDTO.FriendDTO acceptInvite(String inviteCode) {
        return updateInviteStatus(inviteCode, "active", "accepted");
    }

    public FriendsDTO.FriendDTO rejectInvite(String inviteCode) {
        return updateInviteStatus(inviteCode, "rejected", "rejected");
    }

    private FriendsDTO.FriendDTO updateInviteStatus(String inviteCode, String status, String action) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FriendEntity friend = friendRepository.findByInviteCodeAndProfileId(inviteCode, profile.getId())
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        friend.setStatus(status);
        friend = friendRepository.save(friend);
        activity(profile, "invite", friend.getName() + " invite " + action);
        return toFriendDTO(friend, calculateBalances(profile.getId()).getOrDefault(friend.getId(), BigDecimal.ZERO));
    }

    public FriendsDTO.FriendGroupDTO addGroup(FriendsDTO.FriendGroupDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.checkFreeLimit(profile, groupRepository.countByProfileId(profile.getId()), PlanLimitService.FREE_GROUPS, "groups");
        FriendGroupEntity group = new FriendGroupEntity();
        group.setProfile(profile);
        group.setName(dto.name);
        group.setType(dto.type);
        group.setIcon(dto.icon);
        group = groupRepository.save(group);
        saveGroupMembers(group, dto.friendIds, dto.members, profile.getId());
        activity(profile, "group", "Created group " + group.getName());
        return toGroupDTO(group);
    }

    public FriendsDTO.SharedExpenseDTO addExpense(FriendsDTO.SharedExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        enforceSharedExpensePlanRules(profile, dto);
        SharedExpenseEntity expense = new SharedExpenseEntity();
        expense.setProfile(profile);
        expense.setTitle(dto.title);
        expense.setAmount(defaultAmount(dto.amount));
        expense.setExpenseDate(dto.expenseDate);
        expense.setCategory(dto.category);
        expense.setSplitType(dto.splitType);
        expense.setNote(dto.note);
        expense.setReceiptUrl(dto.receiptUrl);
        if (dto.paidByFriendId != null) expense.setPaidByFriend(getFriend(dto.paidByFriendId, profile.getId()));
        if (dto.groupId != null) expense.setGroup(getGroup(dto.groupId, profile.getId()));
        expense = expenseRepository.save(expense);
        saveSplits(expense, dto.splits, profile.getId());
        activity(profile, "expense", "Added shared expense " + expense.getTitle());
        return toExpenseDTO(expense);
    }

    public FriendsDTO.GroupMemberDTO updateGroupRole(Long groupId, Long friendId, String role) {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.requirePro(profile, "group roles");
        getGroup(groupId, profile.getId());
        FriendGroupMemberEntity member = groupMemberRepository.findByGroupIdAndFriendId(groupId, friendId)
                .orElseThrow(() -> new RuntimeException("Group member not found"));
        member.setRole(role == null || role.isBlank() ? "member" : role);
        member = groupMemberRepository.save(member);
        activity(profile, "group", member.getFriend().getName() + " role changed to " + member.getRole());
        return toGroupMemberDTO(member);
    }

    public FriendsDTO.SettlementDTO addSettlement(FriendsDTO.SettlementDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FriendSettlementEntity settlement = new FriendSettlementEntity();
        settlement.setProfile(profile);
        settlement.setFriend(getFriend(dto.friendId, profile.getId()));
        settlement.setAmount(defaultAmount(dto.amount));
        settlement.setSettlementDate(dto.settlementDate);
        settlement.setMethod(dto.method);
        settlement.setDirection(dto.direction);
        settlement.setNote(dto.note);
        settlement = settlementRepository.save(settlement);
        activity(profile, "settlement", "Settlement recorded with " + settlement.getFriend().getName());
        return toSettlementDTO(settlement);
    }

    public FriendsDTO.ReminderDTO addReminder(FriendsDTO.ReminderDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FriendReminderEntity reminder = new FriendReminderEntity();
        reminder.setProfile(profile);
        reminder.setFriend(getFriend(dto.friendId, profile.getId()));
        reminder.setMessage(dto.message);
        reminder.setAmount(defaultAmount(dto.amount));
        reminder.setDueDate(dto.dueDate);
        reminder.setSent(dto.sent);
        reminder = reminderRepository.save(reminder);
        activity(profile, "reminder", "Reminder created for " + reminder.getFriend().getName());
        return toReminderDTO(reminder);
    }

    public FriendsDTO.CommentDTO addComment(Long expenseId, FriendsDTO.CommentDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SharedExpenseEntity expense = expenseRepository.findByIdAndProfileId(expenseId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Shared expense not found"));
        FriendCommentEntity comment = new FriendCommentEntity();
        comment.setExpense(expense);
        comment.setComment(dto.comment);
        comment = commentRepository.save(comment);
        activity(profile, "comment", "Commented on " + expense.getTitle());
        return toCommentDTO(comment);
    }

    public List<FriendsDTO.FriendGroupDTO> getGroups() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<FriendGroupEntity> groups = groupRepository.findByProfileIdOrderByIdDesc(profile.getId());
        List<Long> groupIds = groups.stream().map(FriendGroupEntity::getId).toList();
        Map<Long, List<FriendGroupMemberEntity>> membersByGroupId = groupIds.isEmpty()
                ? Map.of()
                : groupMemberRepository.findByGroupIdInWithFriend(groupIds).stream()
                .collect(Collectors.groupingBy(member -> member.getGroup().getId()));
        return groups.stream().map(group -> toGroupDTO(group, membersByGroupId)).toList();
    }

    public List<FriendsDTO.SharedExpenseDTO> getExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<SharedExpenseEntity> expenses = expenseRepository.findByProfileIdOrderByExpenseDateDesc(profile.getId());
        List<Long> expenseIds = expenses.stream().map(SharedExpenseEntity::getId).toList();
        Map<Long, List<SharedExpenseSplitEntity>> splitsByExpenseId = expenseIds.isEmpty()
                ? Map.of()
                : splitRepository.findByExpenseIdInWithFriend(expenseIds).stream()
                .collect(Collectors.groupingBy(split -> split.getExpense().getId()));
        Map<Long, List<FriendCommentEntity>> commentsByExpenseId = expenseIds.isEmpty()
                ? Map.of()
                : commentRepository.findByExpenseIdInOrderByCreatedAtDesc(expenseIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getExpense().getId()));
        return expenses.stream().map(expense -> toExpenseDTO(expense, splitsByExpenseId, commentsByExpenseId)).toList();
    }

    public List<FriendsDTO.SettlementDTO> getSettlements() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return settlementRepository.findByProfileIdOrderBySettlementDateDesc(profile.getId()).stream().map(this::toSettlementDTO).toList();
    }

    public List<FriendsDTO.ReminderDTO> getReminders() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return reminderRepository.findByProfileIdOrderByDueDateAsc(profile.getId()).stream().map(this::toReminderDTO).toList();
    }

    public List<FriendsDTO.ActivityDTO> getActivities() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return activityRepository.findTop25ByProfileIdOrderByCreatedAtDesc(profile.getId()).stream().map(this::toActivityDTO).toList();
    }

    public List<FriendsDTO.RecurringSharedExpenseDTO> getRecurringExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (!planLimitService.isPro(profile)) return List.of();
        return recurringSharedExpenseRepository.findByProfileIdOrderByNextDueDateAsc(profile.getId()).stream()
                .map(this::toRecurringDTO)
                .toList();
    }

    public FriendsDTO.RecurringSharedExpenseDTO addRecurringExpense(FriendsDTO.RecurringSharedExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.requirePro(profile, "recurring shared expenses");
        RecurringSharedExpenseEntity recurring = new RecurringSharedExpenseEntity();
        recurring.setProfile(profile);
        recurring.setTitle(dto.title);
        recurring.setAmount(defaultAmount(dto.amount));
        recurring.setCategory(dto.category);
        recurring.setSplitType(dto.splitType);
        recurring.setFrequency(dto.frequency);
        recurring.setNextDueDate(dto.nextDueDate);
        recurring.setActive(dto.active);
        recurring.setNote(dto.note);
        recurring.setReceiptUrl(dto.receiptUrl);
        if (dto.paidByFriendId != null) recurring.setPaidByFriend(getFriend(dto.paidByFriendId, profile.getId()));
        if (dto.groupId != null) recurring.setGroup(getGroup(dto.groupId, profile.getId()));
        recurring = recurringSharedExpenseRepository.save(recurring);
        activity(profile, "recurring", "Recurring shared expense created: " + recurring.getTitle());
        return toRecurringDTO(recurring);
    }

    public List<FriendsDTO.SharedExpenseDTO> processDueRecurringExpenses() {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.requirePro(profile, "recurring shared expenses");
        List<FriendsDTO.SharedExpenseDTO> created = new ArrayList<>();
        for (RecurringSharedExpenseEntity recurring : recurringSharedExpenseRepository.findDueByProfileId(profile.getId(), LocalDate.now())) {
            FriendsDTO.SharedExpenseDTO expense = new FriendsDTO.SharedExpenseDTO();
            expense.title = recurring.getTitle();
            expense.amount = recurring.getAmount();
            expense.expenseDate = recurring.getNextDueDate();
            expense.category = recurring.getCategory();
            expense.splitType = recurring.getSplitType();
            expense.note = recurring.getNote();
            expense.receiptUrl = recurring.getReceiptUrl();
            expense.groupId = recurring.getGroup() == null ? null : recurring.getGroup().getId();
            expense.paidByFriendId = recurring.getPaidByFriend() == null ? null : recurring.getPaidByFriend().getId();
            expense.splits = buildRecurringSplits(recurring, profile.getId());
            created.add(addExpense(expense));
            recurring.setNextDueDate(nextDate(recurring.getNextDueDate(), recurring.getFrequency()));
            recurringSharedExpenseRepository.save(recurring);
        }
        return created;
    }

    public List<FriendsDTO.GroupReportDTO> getGroupReports() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return groupRepository.findByProfileIdOrderByIdDesc(profile.getId()).stream()
                .map(group -> groupReport(profile.getId(), group))
                .toList();
    }

    public FriendsDTO.GroupReportDTO getGroupReport(Long groupId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.requirePro(profile, "group reports");
        return groupReport(profile.getId(), getGroup(groupId, profile.getId()));
    }

    public List<FriendsDTO.SettlementSuggestionDTO> getSettlementSuggestions() {
        ProfileEntity profile = profileService.getCurrentProfile();
        planLimitService.requirePro(profile, "AI settlement suggestions");
        Map<Long, BigDecimal> balances = calculateBalances(profile.getId());
        Map<Long, FriendEntity> friendsById = friendRepository.findByProfileIdOrderByNameAsc(profile.getId()).stream()
                .collect(Collectors.toMap(FriendEntity::getId, friend -> friend));
        return balances.entrySet().stream()
                .filter(entry -> entry.getValue().abs().compareTo(new BigDecimal("1")) >= 0)
                .map(entry -> {
                    FriendEntity friend = friendsById.get(entry.getKey());
                    BigDecimal balance = entry.getValue();
                    FriendsDTO.SettlementSuggestionDTO suggestion = new FriendsDTO.SettlementSuggestionDTO();
                    suggestion.friendId = entry.getKey();
                    suggestion.amount = balance.abs().setScale(2, RoundingMode.HALF_UP);
                    if (balance.compareTo(BigDecimal.ZERO) > 0) {
                        suggestion.fromName = friend.getName();
                        suggestion.toName = "You";
                    } else {
                        suggestion.fromName = "You";
                        suggestion.toName = friend.getName();
                    }
                    suggestion.note = "Smart settlement suggestion based on current net balance.";
                    return suggestion;
                })
                .toList();
    }

    private void saveSplits(SharedExpenseEntity expense, List<FriendsDTO.SplitDTO> splits, Long profileId) {
        if (splits == null || splits.isEmpty()) return;
        BigDecimal total = defaultAmount(expense.getAmount());
        String splitType = expense.getSplitType() == null ? "equal" : expense.getSplitType();
        BigDecimal totalShares = splits.stream().map(split -> defaultAmount(split.shares)).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal equalAmount = total.divide(new BigDecimal(splits.size() + 1), 2, RoundingMode.HALF_UP);

        for (FriendsDTO.SplitDTO splitDTO : splits) {
            FriendEntity friend = getFriend(splitDTO.friendId, profileId);
            SharedExpenseSplitEntity split = new SharedExpenseSplitEntity();
            split.setExpense(expense);
            split.setFriend(friend);
            split.setPercentValue(splitDTO.percentValue);
            split.setShares(splitDTO.shares);
            if ("equal".equalsIgnoreCase(splitType)) {
                split.setAmount(equalAmount);
            } else if ("percentage".equalsIgnoreCase(splitType)) {
                split.setAmount(total.multiply(defaultAmount(splitDTO.percentValue)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
            } else if ("shares".equalsIgnoreCase(splitType) && totalShares.compareTo(BigDecimal.ZERO) > 0) {
                split.setAmount(total.multiply(defaultAmount(splitDTO.shares)).divide(totalShares.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP));
            } else {
                split.setAmount(defaultAmount(splitDTO.amount));
            }
            splitRepository.save(split);
        }
    }

    private Map<Long, BigDecimal> calculateBalances(Long profileId) {
        Map<Long, BigDecimal> balances = friendRepository.findByProfileIdOrderByNameAsc(profileId).stream()
                .collect(Collectors.toMap(FriendEntity::getId, friend -> defaultAmount(friend.getOpeningBalance())));
        List<SharedExpenseSplitEntity> splits = splitRepository.findByExpenseProfileId(profileId);
        Map<Long, BigDecimal> splitTotalsByExpenseId = splits.stream()
                .collect(Collectors.groupingBy(split -> split.getExpense().getId(),
                        Collectors.mapping(split -> defaultAmount(split.getAmount()),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        for (SharedExpenseSplitEntity split : splits) {
            Long friendId = split.getFriend().getId();
            SharedExpenseEntity expense = split.getExpense();
            BigDecimal amount = defaultAmount(split.getAmount());
            if (expense.getPaidByFriend() == null) {
                balances.merge(friendId, amount, BigDecimal::add);
            } else if (expense.getPaidByFriend().getId().equals(friendId)) {
                BigDecimal userShare = defaultAmount(expense.getAmount()).subtract(splitTotalsByExpenseId.getOrDefault(expense.getId(), BigDecimal.ZERO));
                balances.merge(friendId, userShare.negate(), BigDecimal::add);
            }
        }
        for (FriendSettlementEntity settlement : settlementRepository.findByProfileIdOrderBySettlementDateDesc(profileId)) {
            BigDecimal amount = defaultAmount(settlement.getAmount());
            if ("you_paid_friend".equalsIgnoreCase(settlement.getDirection())) {
                balances.merge(settlement.getFriend().getId(), amount, BigDecimal::add);
            } else {
                balances.merge(settlement.getFriend().getId(), amount.negate(), BigDecimal::add);
            }
        }
        return balances;
    }

    private BigDecimal totalSplitAmount(Long expenseId) {
        return splitRepository.findByExpenseId(expenseId).stream().map(SharedExpenseSplitEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void saveGroupMembers(FriendGroupEntity group, List<Long> friendIds, List<FriendsDTO.GroupMemberDTO> members, Long profileId) {
        if (members != null && !members.isEmpty()) {
            for (FriendsDTO.GroupMemberDTO memberDTO : members) {
                saveGroupMember(group, memberDTO.friendId, memberDTO.role, profileId);
            }
            return;
        }
        if (friendIds == null) return;
        for (Long friendId : friendIds) {
            saveGroupMember(group, friendId, "member", profileId);
        }
    }

    private void saveGroupMember(FriendGroupEntity group, Long friendId, String role, Long profileId) {
        if (friendId == null || groupMemberRepository.existsByGroupIdAndFriendId(group.getId(), friendId)) return;
        FriendGroupMemberEntity member = new FriendGroupMemberEntity();
        member.setGroup(group);
        member.setFriend(getFriend(friendId, profileId));
        member.setRole(role == null || role.isBlank() ? "member" : role);
        groupMemberRepository.save(member);
    }

    private void enforceSharedExpensePlanRules(ProfileEntity profile, FriendsDTO.SharedExpenseDTO dto) {
        planLimitService.checkFreeLimit(
                profile,
                sharedExpenseCountThisMonth(profile.getId()),
                PlanLimitService.FREE_SHARED_EXPENSES_PER_MONTH,
                "monthly shared expenses"
        );
        String splitType = dto.splitType == null || dto.splitType.isBlank() ? "equal" : dto.splitType;
        if (!"equal".equalsIgnoreCase(splitType)) {
            planLimitService.requirePro(profile, "advanced split types");
        }
        if (dto.receiptUrl != null && !dto.receiptUrl.isBlank()) {
            planLimitService.requirePro(profile, "receipt upload");
        }
    }

    private long sharedExpenseCountThisMonth(Long profileId) {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
        return expenseRepository.countByProfileIdAndExpenseDateBetween(profileId, start, end);
    }

    private List<FriendsDTO.SplitDTO> buildRecurringSplits(RecurringSharedExpenseEntity recurring, Long profileId) {
        if (recurring.getGroup() == null) return List.of();
        return groupMemberRepository.findByGroupId(recurring.getGroup().getId()).stream()
                .map(member -> {
                    FriendsDTO.SplitDTO split = new FriendsDTO.SplitDTO();
                    split.friendId = member.getFriend().getId();
                    return split;
                })
                .toList();
    }

    private LocalDate nextDate(LocalDate current, String frequency) {
        LocalDate date = current == null ? LocalDate.now() : current;
        if ("weekly".equalsIgnoreCase(frequency)) return date.plusWeeks(1);
        if ("yearly".equalsIgnoreCase(frequency)) return date.plusYears(1);
        return date.plusMonths(1);
    }

    private FriendsDTO.GroupReportDTO groupReport(Long profileId, FriendGroupEntity group) {
        List<SharedExpenseEntity> expenses = expenseRepository.findByProfileIdAndGroupId(profileId, group.getId());
        List<FriendGroupMemberEntity> members = groupMemberRepository.findByGroupId(group.getId());
        FriendsDTO.GroupReportDTO report = new FriendsDTO.GroupReportDTO();
        report.groupId = group.getId();
        report.groupName = group.getName();
        report.expenseCount = (long) expenses.size();
        report.memberCount = (long) members.size();
        report.totalSpent = expenses.stream().map(SharedExpenseEntity::getAmount).map(this::defaultAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        report.settlementTotal = settlementRepository.findByProfileIdOrderBySettlementDateDesc(profileId).stream()
                .filter(settlement -> members.stream().anyMatch(member -> member.getFriend().getId().equals(settlement.getFriend().getId())))
                .map(FriendSettlementEntity::getAmount)
                .map(this::defaultAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.pendingBalance = report.totalSpent.subtract(report.settlementTotal).max(BigDecimal.ZERO);
        report.topCategory = expenses.stream()
                .filter(expense -> expense.getCategory() != null)
                .collect(Collectors.groupingBy(SharedExpenseEntity::getCategory, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("General");
        report.topPayerName = expenses.stream()
                .filter(expense -> expense.getPaidByFriend() != null)
                .collect(Collectors.groupingBy(expense -> expense.getPaidByFriend().getName(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("You");
        return report;
    }

    private FriendEntity getFriend(Long id, Long profileId) {
        return friendRepository.findByIdAndProfileId(id, profileId).orElseThrow(() -> new RuntimeException("Friend not found"));
    }

    private FriendGroupEntity getGroup(Long id, Long profileId) {
        return groupRepository.findByIdAndProfileId(id, profileId).orElseThrow(() -> new RuntimeException("Group not found"));
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private void activity(ProfileEntity profile, String type, String message) {
        FriendActivityEntity activity = new FriendActivityEntity();
        activity.setProfile(profile);
        activity.setType(type);
        activity.setMessage(message);
        activityRepository.save(activity);
        friendLiveEventService.publish(profile, type, message);
    }

    private FriendsDTO.FriendDTO toFriendDTO(FriendEntity entity, BigDecimal balance) {
        FriendsDTO.FriendDTO dto = new FriendsDTO.FriendDTO();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.email = entity.getEmail();
        dto.phone = entity.getPhone();
        dto.avatarUrl = entity.getAvatarUrl();
        dto.upiId = entity.getUpiId();
        dto.status = entity.getStatus();
        dto.inviteCode = entity.getInviteCode();
        dto.inviteLink = frontendUrl + "/friends/invite/" + entity.getInviteCode();
        dto.balance = balance;
        return dto;
    }

    private FriendsDTO.FriendGroupDTO toGroupDTO(FriendGroupEntity entity) {
        return toGroupDTO(entity, Map.of(entity.getId(), groupMemberRepository.findByGroupId(entity.getId())));
    }

    private FriendsDTO.FriendGroupDTO toGroupDTO(FriendGroupEntity entity, Map<Long, List<FriendGroupMemberEntity>> membersByGroupId) {
        FriendsDTO.FriendGroupDTO dto = new FriendsDTO.FriendGroupDTO();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.type = entity.getType();
        dto.icon = entity.getIcon();
        List<FriendGroupMemberEntity> members = membersByGroupId.getOrDefault(entity.getId(), List.of());
        dto.friendIds = members.stream().map(member -> member.getFriend().getId()).toList();
        dto.members = members.stream().map(this::toGroupMemberDTO).toList();
        return dto;
    }

    private FriendsDTO.GroupMemberDTO toGroupMemberDTO(FriendGroupMemberEntity entity) {
        FriendsDTO.GroupMemberDTO dto = new FriendsDTO.GroupMemberDTO();
        dto.friendId = entity.getFriend().getId();
        dto.friendName = entity.getFriend().getName();
        dto.role = entity.getRole() == null ? "member" : entity.getRole();
        return dto;
    }

    private FriendsDTO.SharedExpenseDTO toExpenseDTO(SharedExpenseEntity entity) {
        return toExpenseDTO(entity,
                Map.of(entity.getId(), splitRepository.findByExpenseId(entity.getId())),
                Map.of(entity.getId(), commentRepository.findByExpenseIdOrderByCreatedAtDesc(entity.getId())));
    }

    private FriendsDTO.SharedExpenseDTO toExpenseDTO(
            SharedExpenseEntity entity,
            Map<Long, List<SharedExpenseSplitEntity>> splitsByExpenseId,
            Map<Long, List<FriendCommentEntity>> commentsByExpenseId
    ) {
        FriendsDTO.SharedExpenseDTO dto = new FriendsDTO.SharedExpenseDTO();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.amount = entity.getAmount();
        dto.expenseDate = entity.getExpenseDate();
        dto.category = entity.getCategory();
        dto.splitType = entity.getSplitType();
        dto.note = entity.getNote();
        dto.receiptUrl = entity.getReceiptUrl();
        if (entity.getPaidByFriend() != null) {
            dto.paidByFriendId = entity.getPaidByFriend().getId();
            dto.paidByFriendName = entity.getPaidByFriend().getName();
        }
        if (entity.getGroup() != null) {
            dto.groupId = entity.getGroup().getId();
            dto.groupName = entity.getGroup().getName();
        }
        dto.splits = splitsByExpenseId.getOrDefault(entity.getId(), List.of()).stream().map(this::toSplitDTO).toList();
        dto.comments = commentsByExpenseId.getOrDefault(entity.getId(), List.of()).stream().map(this::toCommentDTO).toList();
        return dto;
    }

    private FriendsDTO.RecurringSharedExpenseDTO toRecurringDTO(RecurringSharedExpenseEntity entity) {
        FriendsDTO.RecurringSharedExpenseDTO dto = new FriendsDTO.RecurringSharedExpenseDTO();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.amount = entity.getAmount();
        dto.category = entity.getCategory();
        dto.splitType = entity.getSplitType();
        dto.frequency = entity.getFrequency();
        dto.nextDueDate = entity.getNextDueDate();
        dto.active = entity.getActive();
        dto.note = entity.getNote();
        dto.receiptUrl = entity.getReceiptUrl();
        if (entity.getPaidByFriend() != null) {
            dto.paidByFriendId = entity.getPaidByFriend().getId();
            dto.paidByFriendName = entity.getPaidByFriend().getName();
        }
        if (entity.getGroup() != null) {
            dto.groupId = entity.getGroup().getId();
            dto.groupName = entity.getGroup().getName();
        }
        return dto;
    }

    private FriendsDTO.SplitDTO toSplitDTO(SharedExpenseSplitEntity entity) {
        FriendsDTO.SplitDTO dto = new FriendsDTO.SplitDTO();
        dto.friendId = entity.getFriend().getId();
        dto.friendName = entity.getFriend().getName();
        dto.amount = entity.getAmount();
        dto.percentValue = entity.getPercentValue();
        dto.shares = entity.getShares();
        return dto;
    }

    private FriendsDTO.SettlementDTO toSettlementDTO(FriendSettlementEntity entity) {
        FriendsDTO.SettlementDTO dto = new FriendsDTO.SettlementDTO();
        dto.id = entity.getId();
        dto.friendId = entity.getFriend().getId();
        dto.friendName = entity.getFriend().getName();
        dto.amount = entity.getAmount();
        dto.settlementDate = entity.getSettlementDate();
        dto.method = entity.getMethod();
        dto.direction = entity.getDirection();
        dto.note = entity.getNote();
        return dto;
    }

    private FriendsDTO.ReminderDTO toReminderDTO(FriendReminderEntity entity) {
        FriendsDTO.ReminderDTO dto = new FriendsDTO.ReminderDTO();
        dto.id = entity.getId();
        dto.friendId = entity.getFriend().getId();
        dto.friendName = entity.getFriend().getName();
        dto.message = entity.getMessage();
        dto.amount = entity.getAmount();
        dto.dueDate = entity.getDueDate();
        dto.sent = entity.getSent();
        return dto;
    }

    private FriendsDTO.CommentDTO toCommentDTO(FriendCommentEntity entity) {
        FriendsDTO.CommentDTO dto = new FriendsDTO.CommentDTO();
        dto.id = entity.getId();
        dto.expenseId = entity.getExpense().getId();
        dto.comment = entity.getComment();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

    private FriendsDTO.ActivityDTO toActivityDTO(FriendActivityEntity entity) {
        FriendsDTO.ActivityDTO dto = new FriendsDTO.ActivityDTO();
        dto.id = entity.getId();
        dto.type = entity.getType();
        dto.message = entity.getMessage();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }
}
