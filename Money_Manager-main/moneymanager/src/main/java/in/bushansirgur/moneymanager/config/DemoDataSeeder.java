package in.bushansirgur.moneymanager.config;

import in.bushansirgur.moneymanager.entity.*;
import in.bushansirgur.moneymanager.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    private static final String DEMO_EMAIL = "demo@moneymanager.local";
    private static final String DEMO_INVITE_CODE = "DEMO2026";

    private final ProfileRepository profileRepository;
    private final CategoryRepository categoryRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyTransferRepository familyTransferRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final FriendRepository friendRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final FriendGroupMemberRepository friendGroupMemberRepository;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final SharedExpenseSplitRepository sharedExpenseSplitRepository;
    private final FriendSettlementRepository friendSettlementRepository;
    private final FriendReminderRepository friendReminderRepository;
    private final FriendCommentRepository friendCommentRepository;
    private final FriendActivityRepository friendActivityRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(ProfileRepository profileRepository,
                          CategoryRepository categoryRepository,
                          FamilyRepository familyRepository,
                          FamilyMemberRepository familyMemberRepository,
                          FamilyTransferRepository familyTransferRepository,
                          IncomeRepository incomeRepository,
                          ExpenseRepository expenseRepository,
                          FriendRepository friendRepository,
                          FriendGroupRepository friendGroupRepository,
                          FriendGroupMemberRepository friendGroupMemberRepository,
                          SharedExpenseRepository sharedExpenseRepository,
                          SharedExpenseSplitRepository sharedExpenseSplitRepository,
                          FriendSettlementRepository friendSettlementRepository,
                          FriendReminderRepository friendReminderRepository,
                          FriendCommentRepository friendCommentRepository,
                          FriendActivityRepository friendActivityRepository,
                          PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.categoryRepository = categoryRepository;
        this.familyRepository = familyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.familyTransferRepository = familyTransferRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.friendRepository = friendRepository;
        this.friendGroupRepository = friendGroupRepository;
        this.friendGroupMemberRepository = friendGroupMemberRepository;
        this.sharedExpenseRepository = sharedExpenseRepository;
        this.sharedExpenseSplitRepository = sharedExpenseSplitRepository;
        this.friendSettlementRepository = friendSettlementRepository;
        this.friendReminderRepository = friendReminderRepository;
        this.friendCommentRepository = friendCommentRepository;
        this.friendActivityRepository = friendActivityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ProfileEntity profile = profileRepository.findByEmail(DEMO_EMAIL).orElseGet(this::createDemoProfile);
        seedFriendsDemoData(profile);
        if (familyRepository.findByInviteCode(DEMO_INVITE_CODE).isPresent()) {
            return;
        }

        CategoryEntity salary = findOrCreateCategory(profile, "Salary", "income", "💼");
        CategoryEntity gifts = findOrCreateCategory(profile, "Family Support", "income", "🏦");
        CategoryEntity groceries = findOrCreateCategory(profile, "Groceries", "expense", "🛒");
        CategoryEntity education = findOrCreateCategory(profile, "Education", "expense", "📚");
        CategoryEntity shopping = findOrCreateCategory(profile, "Shopping", "expense", "🛍️");
        CategoryEntity transport = findOrCreateCategory(profile, "Transport", "expense", "🚌");
        CategoryEntity bills = findOrCreateCategory(profile, "Bills", "expense", "💡");

        FamilyEntity family = new FamilyEntity();
        family.setName("Kumar Family");
        family.setInviteCode(DEMO_INVITE_CODE);
        family.setOwner(profile);
        family = familyRepository.save(family);

        FamilyMemberEntity father = createMember(family, profile, "Father", "Parent", "father", "0");
        FamilyMemberEntity mother = createMember(family, null, "Mother", "Parent", "mother", "0");
        FamilyMemberEntity son = createMember(family, null, "Son", "Child", "son", "6000");
        FamilyMemberEntity daughter = createMember(family, null, "Daughter", "Child", "daughter", "5000");
        FamilyMemberEntity home = createMember(family, null, "Home Bills", "Household", "home", "18000");

        LocalDate now = LocalDate.now();
        addIncome(profile, father, salary, "Father salary", "💼", "85000", now.withDayOfMonth(1), "bank", "monthly,family", "Primary monthly salary");
        addIncome(profile, mother, salary, "Mother business income", "🏪", "32000", now.withDayOfMonth(3), "UPI", "business,family", "Home business income");
        addIncome(profile, home, gifts, "Grandparent support", "🏦", "7000", now.withDayOfMonth(5), "bank", "support,family", "Support added to household wallet");

        addExpense(profile, son, education, "Son books and notes", "📚", "1850", now.withDayOfMonth(6), "UPI", "school,son", "Reference books and stationery");
        addExpense(profile, son, transport, "Son college bus pass", "🚌", "1200", now.withDayOfMonth(8), "card", "college,transport", "Monthly commute pass");
        addExpense(profile, daughter, shopping, "Daughter clothes", "🛍️", "2400", now.withDayOfMonth(9), "UPI", "daughter,shopping", "Festival outfit purchase");
        addExpense(profile, daughter, education, "Daughter tuition fee", "📚", "3000", now.withDayOfMonth(10), "bank", "tuition,daughter", "Math tuition installment");
        addExpense(profile, mother, groceries, "Weekly groceries", "🛒", "4650", now.withDayOfMonth(11), "UPI", "home,grocery", "Family grocery run");
        addExpense(profile, home, bills, "Electricity bill", "💡", "2850", now.withDayOfMonth(12), "bank", "bill,home", "Monthly electricity payment");
        addExpense(profile, home, bills, "Internet subscription", "🌐", "999", now.withDayOfMonth(14), "card", "bill,internet", "Family broadband bill");

        addTransfer(family, father, son, "5000", now.withDayOfMonth(4), "Monthly allowance for college");
        addTransfer(family, father, daughter, "4500", now.withDayOfMonth(4), "Monthly allowance for school");
        addTransfer(family, father, mother, "15000", now.withDayOfMonth(5), "Groceries and home running cash");
        addTransfer(family, mother, home, "6000", now.withDayOfMonth(13), "Bills paid from household wallet");
    }

    private ProfileEntity createDemoProfile() {
        ProfileEntity profile = new ProfileEntity();
        profile.setFullName("Demo Family User");
        profile.setEmail(DEMO_EMAIL);
        profile.setPassword(passwordEncoder.encode("Demo@12345"));
        profile.setActive(true);
        profile.setProfileImageUrl("https://api.dicebear.com/8.x/initials/svg?seed=Demo%20Family");
        return profileRepository.save(profile);
    }

    private CategoryEntity findOrCreateCategory(ProfileEntity profile, String name, String type, String icon) {
        List<CategoryEntity> categories = categoryRepository.findByTypeAndProfileId(type, profile.getId());
        return categories.stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    CategoryEntity category = new CategoryEntity();
                    category.setProfile(profile);
                    category.setName(name);
                    category.setType(type);
                    category.setIcon(icon);
                    return categoryRepository.save(category);
                });
    }

    private void seedFriendsDemoData(ProfileEntity profile) {
        List<FriendEntity> existingFriends = friendRepository.findByProfileIdOrderByNameAsc(profile.getId());
        FriendEntity rahul = findOrCreateFriend(profile, existingFriends, "Rahul Sharma", "rahul@example.com", "9999999999", "rahul@upi", "active");
        FriendEntity priya = findOrCreateFriend(profile, existingFriends, "Priya Mehta", "priya@example.com", "8888888888", "priya@upi", "active");
        FriendEntity ankit = findOrCreateFriend(profile, existingFriends, "Ankit Verma", "ankit@example.com", "7777777777", "ankit@upi", "invited");
        FriendEntity sneha = findOrCreateFriend(profile, existingFriends, "Sneha Rao", "sneha@example.com", "6666666666", "sneha@upi", "active");
        FriendEntity rohan = findOrCreateFriend(profile, existingFriends, "Rohan Das", "rohan@example.com", "5555555555", "rohan@upi", "active");

        if (!sharedExpenseRepository.findByProfileIdOrderByExpenseDateDesc(profile.getId()).isEmpty()) {
            return;
        }

        FriendGroupEntity goaTrip = createGroup(profile, "Goa Trip", "Trip", "GT", List.of(rahul, priya, ankit, sneha));
        FriendGroupEntity flatmates = createGroup(profile, "Flatmates", "Roommates", "FL", List.of(rahul, rohan));
        FriendGroupEntity officeLunch = createGroup(profile, "Office Lunch", "Office Lunch", "OL", List.of(priya, ankit, sneha, rohan));

        LocalDate now = LocalDate.now();
        SharedExpenseEntity hotel = createSharedExpense(profile, "Goa hotel advance", "18000", now.minusDays(11), "Travel", "equal", null, goaTrip, "Beachside hotel booking advance");
        createSplit(hotel, rahul, "3600", null, null);
        createSplit(hotel, priya, "3600", null, null);
        createSplit(hotel, ankit, "3600", null, null);
        createSplit(hotel, sneha, "3600", null, null);
        createComment(hotel, "Booking confirmed. Rahul will collect everyone before check-in.");

        SharedExpenseEntity dinner = createSharedExpense(profile, "Birthday dinner", "6200", now.minusDays(7), "Food", "percentage", priya, null, "Priya paid for the group dinner");
        createSplit(dinner, rahul, "1550", "25", null);
        createSplit(dinner, ankit, "1240", "20", null);
        createSplit(dinner, sneha, "1860", "30", null);
        createSplit(dinner, rohan, "1550", "25", null);
        createComment(dinner, "Cake amount included in dinner bill.");

        SharedExpenseEntity rent = createSharedExpense(profile, "Flat electricity bill", "3600", now.minusDays(5), "Bills", "exact", rohan, flatmates, "Shared apartment electricity bill");
        createSplit(rent, rahul, "1800", null, null);
        createComment(rent, "Bill paid from Rohan card.");

        SharedExpenseEntity cab = createSharedExpense(profile, "Airport cab", "2400", now.minusDays(3), "Transport", "shares", null, goaTrip, "Cab from airport to hotel");
        createSplit(cab, rahul, "600", null, "1");
        createSplit(cab, priya, "600", null, "1");
        createSplit(cab, ankit, "600", null, "1");
        createSplit(cab, sneha, "600", null, "1");

        SharedExpenseEntity lunch = createSharedExpense(profile, "Friday team lunch", "4200", now.minusDays(2), "Food", "equal", ankit, officeLunch, "Office lunch at cafe");
        createSplit(lunch, priya, "840", null, null);
        createSplit(lunch, ankit, "840", null, null);
        createSplit(lunch, sneha, "840", null, null);
        createSplit(lunch, rohan, "840", null, null);

        createSettlement(profile, rahul, "2500", now.minusDays(4), "UPI", "friend_paid_you", "Rahul paid Goa hotel share partly");
        createSettlement(profile, priya, "1000", now.minusDays(1), "UPI", "you_paid_friend", "Paid Priya back for dinner");
        createSettlement(profile, rohan, "900", now.minusDays(1), "cash", "friend_paid_you", "Rohan settled half of cab and snacks");

        createReminder(profile, ankit, "Ankit still owes for Goa hotel and lunch", "4440", now.plusDays(2));
        createReminder(profile, sneha, "Send reminder for dinner split", "2700", now.plusDays(4));
        createReminder(profile, rahul, "Rahul remaining Goa settlement", "1100", now.plusDays(3));

        createActivity(profile, "friend", "Added 5 demo friends for split expenses");
        createActivity(profile, "group", "Created Goa Trip, Flatmates, and Office Lunch groups");
        createActivity(profile, "expense", "Added 5 shared expenses with equal, exact, percentage, and shares splits");
        createActivity(profile, "settlement", "Recorded 3 demo settlements");
        createActivity(profile, "reminder", "Created 3 payment reminders");
    }

    private FriendEntity findOrCreateFriend(ProfileEntity profile, List<FriendEntity> existingFriends, String name, String email, String phone, String upiId, String status) {
        return existingFriends.stream()
                .filter(friend -> friend.getEmail() != null && friend.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseGet(() -> {
                    FriendEntity friend = new FriendEntity();
                    friend.setProfile(profile);
                    friend.setName(name);
                    friend.setEmail(email);
                    friend.setPhone(phone);
                    friend.setUpiId(upiId);
                    friend.setStatus(status);
                    friend.setAvatarUrl("https://api.dicebear.com/8.x/initials/svg?seed=" + name.replace(" ", "%20"));
                    friend.setOpeningBalance(BigDecimal.ZERO);
                    return friendRepository.save(friend);
                });
    }

    private FriendGroupEntity createGroup(ProfileEntity profile, String name, String type, String icon, List<FriendEntity> friends) {
        FriendGroupEntity group = new FriendGroupEntity();
        group.setProfile(profile);
        group.setName(name);
        group.setType(type);
        group.setIcon(icon);
        group = friendGroupRepository.save(group);
        for (FriendEntity friend : friends) {
            FriendGroupMemberEntity member = new FriendGroupMemberEntity();
            member.setGroup(group);
            member.setFriend(friend);
            friendGroupMemberRepository.save(member);
        }
        return group;
    }

    private SharedExpenseEntity createSharedExpense(ProfileEntity profile, String title, String amount, LocalDate date, String category, String splitType, FriendEntity paidByFriend, FriendGroupEntity group, String note) {
        SharedExpenseEntity expense = new SharedExpenseEntity();
        expense.setProfile(profile);
        expense.setTitle(title);
        expense.setAmount(new BigDecimal(amount));
        expense.setExpenseDate(date);
        expense.setCategory(category);
        expense.setSplitType(splitType);
        expense.setPaidByFriend(paidByFriend);
        expense.setGroup(group);
        expense.setNote(note);
        return sharedExpenseRepository.save(expense);
    }

    private void createSplit(SharedExpenseEntity expense, FriendEntity friend, String amount, String percent, String shares) {
        SharedExpenseSplitEntity split = new SharedExpenseSplitEntity();
        split.setExpense(expense);
        split.setFriend(friend);
        split.setAmount(new BigDecimal(amount));
        split.setPercentValue(percent == null ? null : new BigDecimal(percent));
        split.setShares(shares == null ? null : new BigDecimal(shares));
        sharedExpenseSplitRepository.save(split);
    }

    private void createSettlement(ProfileEntity profile, FriendEntity friend, String amount, LocalDate date, String method, String direction, String note) {
        FriendSettlementEntity settlement = new FriendSettlementEntity();
        settlement.setProfile(profile);
        settlement.setFriend(friend);
        settlement.setAmount(new BigDecimal(amount));
        settlement.setSettlementDate(date);
        settlement.setMethod(method);
        settlement.setDirection(direction);
        settlement.setNote(note);
        friendSettlementRepository.save(settlement);
    }

    private void createReminder(ProfileEntity profile, FriendEntity friend, String message, String amount, LocalDate dueDate) {
        FriendReminderEntity reminder = new FriendReminderEntity();
        reminder.setProfile(profile);
        reminder.setFriend(friend);
        reminder.setMessage(message);
        reminder.setAmount(new BigDecimal(amount));
        reminder.setDueDate(dueDate);
        reminder.setSent(false);
        friendReminderRepository.save(reminder);
    }

    private void createComment(SharedExpenseEntity expense, String text) {
        FriendCommentEntity comment = new FriendCommentEntity();
        comment.setExpense(expense);
        comment.setComment(text);
        friendCommentRepository.save(comment);
    }

    private void createActivity(ProfileEntity profile, String type, String message) {
        FriendActivityEntity activity = new FriendActivityEntity();
        activity.setProfile(profile);
        activity.setType(type);
        activity.setMessage(message);
        friendActivityRepository.save(activity);
    }

    private FamilyMemberEntity createMember(FamilyEntity family, ProfileEntity profile, String name, String role, String avatar, String allowance) {
        FamilyMemberEntity member = new FamilyMemberEntity();
        member.setFamily(family);
        member.setProfile(profile);
        member.setName(name);
        member.setRole(role);
        member.setAvatar(avatar);
        member.setMonthlyAllowance(new BigDecimal(allowance));
        return familyMemberRepository.save(member);
    }

    private void addIncome(ProfileEntity profile, FamilyMemberEntity member, CategoryEntity category, String name, String icon,
                           String amount, LocalDate date, String paymentMethod, String tags, String notes) {
        IncomeEntity income = new IncomeEntity();
        income.setProfile(profile);
        income.setFamilyMember(member);
        income.setCategory(category);
        income.setName(name);
        income.setIcon(icon);
        income.setAmount(new BigDecimal(amount));
        income.setDate(date);
        income.setPaymentMethod(paymentMethod);
        income.setTags(tags);
        income.setNotes(notes);
        incomeRepository.save(income);
    }

    private void addExpense(ProfileEntity profile, FamilyMemberEntity member, CategoryEntity category, String name, String icon,
                            String amount, LocalDate date, String paymentMethod, String tags, String notes) {
        ExpenseEntity expense = new ExpenseEntity();
        expense.setProfile(profile);
        expense.setFamilyMember(member);
        expense.setCategory(category);
        expense.setName(name);
        expense.setIcon(icon);
        expense.setAmount(new BigDecimal(amount));
        expense.setDate(date);
        expense.setPaymentMethod(paymentMethod);
        expense.setTags(tags);
        expense.setNotes(notes);
        expenseRepository.save(expense);
    }

    private void addTransfer(FamilyEntity family, FamilyMemberEntity from, FamilyMemberEntity to, String amount, LocalDate date, String note) {
        FamilyTransferEntity transfer = new FamilyTransferEntity();
        transfer.setFamily(family);
        transfer.setFromMember(from);
        transfer.setToMember(to);
        transfer.setAmount(new BigDecimal(amount));
        transfer.setTransferDate(date);
        transfer.setNote(note);
        familyTransferRepository.save(transfer);
    }
}
