package in.bushansirgur.moneymanager.config;

import in.bushansirgur.moneymanager.entity.CategoryEntity;
import in.bushansirgur.moneymanager.entity.ExpenseEntity;
import in.bushansirgur.moneymanager.entity.FamilyEntity;
import in.bushansirgur.moneymanager.entity.FamilyMemberEntity;
import in.bushansirgur.moneymanager.entity.FamilyTransferEntity;
import in.bushansirgur.moneymanager.entity.IncomeEntity;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.CategoryRepository;
import in.bushansirgur.moneymanager.repository.ExpenseRepository;
import in.bushansirgur.moneymanager.repository.FamilyMemberRepository;
import in.bushansirgur.moneymanager.repository.FamilyRepository;
import in.bushansirgur.moneymanager.repository.FamilyTransferRepository;
import in.bushansirgur.moneymanager.repository.IncomeRepository;
import in.bushansirgur.moneymanager.repository.ProfileRepository;
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
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(ProfileRepository profileRepository,
                          CategoryRepository categoryRepository,
                          FamilyRepository familyRepository,
                          FamilyMemberRepository familyMemberRepository,
                          FamilyTransferRepository familyTransferRepository,
                          IncomeRepository incomeRepository,
                          ExpenseRepository expenseRepository,
                          PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.categoryRepository = categoryRepository;
        this.familyRepository = familyRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.familyTransferRepository = familyTransferRepository;
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ProfileEntity profile = profileRepository.findByEmail(DEMO_EMAIL).orElseGet(this::createDemoProfile);
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
