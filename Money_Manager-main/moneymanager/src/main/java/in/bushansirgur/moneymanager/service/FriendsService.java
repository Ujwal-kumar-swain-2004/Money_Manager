package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.FriendsDTO;
import in.bushansirgur.moneymanager.entity.*;
import in.bushansirgur.moneymanager.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        dto.setTotalOwedToYou(friends.stream().map(friend -> friend.balance.max(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setTotalYouOwe(friends.stream().map(friend -> friend.balance.min(BigDecimal.ZERO).abs()).reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setNetBalance(dto.getTotalOwedToYou().subtract(dto.getTotalYouOwe()));
        return dto;
    }

    public FriendsDTO.FriendDTO addFriend(FriendsDTO.FriendDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
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

    public FriendsDTO.FriendDTO updateFriendStatus(Long friendId, String status) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FriendEntity friend = getFriend(friendId, profile.getId());
        friend.setStatus(status);
        friend = friendRepository.save(friend);
        activity(profile, "friend", friend.getName() + " marked as " + status);
        return toFriendDTO(friend, calculateBalances(profile.getId()).getOrDefault(friend.getId(), BigDecimal.ZERO));
    }

    public FriendsDTO.FriendGroupDTO addGroup(FriendsDTO.FriendGroupDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        FriendGroupEntity group = new FriendGroupEntity();
        group.setProfile(profile);
        group.setName(dto.name);
        group.setType(dto.type);
        group.setIcon(dto.icon);
        group = groupRepository.save(group);
        saveGroupMembers(group, dto.friendIds, profile.getId());
        activity(profile, "group", "Created group " + group.getName());
        return toGroupDTO(group);
    }

    public FriendsDTO.SharedExpenseDTO addExpense(FriendsDTO.SharedExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SharedExpenseEntity expense = new SharedExpenseEntity();
        expense.setProfile(profile);
        expense.setTitle(dto.title);
        expense.setAmount(defaultAmount(dto.amount));
        expense.setExpenseDate(dto.expenseDate);
        expense.setCategory(dto.category);
        expense.setSplitType(dto.splitType);
        expense.setNote(dto.note);
        if (dto.paidByFriendId != null) expense.setPaidByFriend(getFriend(dto.paidByFriendId, profile.getId()));
        if (dto.groupId != null) expense.setGroup(getGroup(dto.groupId, profile.getId()));
        expense = expenseRepository.save(expense);
        saveSplits(expense, dto.splits, profile.getId());
        activity(profile, "expense", "Added shared expense " + expense.getTitle());
        return toExpenseDTO(expense);
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

    private void saveGroupMembers(FriendGroupEntity group, List<Long> friendIds, Long profileId) {
        if (friendIds == null) return;
        for (Long friendId : friendIds) {
            if (groupMemberRepository.existsByGroupIdAndFriendId(group.getId(), friendId)) continue;
            FriendGroupMemberEntity member = new FriendGroupMemberEntity();
            member.setGroup(group);
            member.setFriend(getFriend(friendId, profileId));
            groupMemberRepository.save(member);
        }
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
        dto.friendIds = membersByGroupId.getOrDefault(entity.getId(), List.of()).stream().map(member -> member.getFriend().getId()).toList();
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
