package in.bushansirgur.moneymanager.service;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonalFinanceKnowledgeService {

    private static final List<KnowledgeSnippet> KNOWLEDGE_BASE = List.of(
            new KnowledgeSnippet(
                    "Emergency fund rule",
                    Set.of("emergency", "risk", "safety", "job", "medical", "buffer"),
                    "Keep an emergency fund before aggressive investing. A practical target is 3 to 6 months of essential expenses, kept in a liquid savings account, sweep account, or liquid fund."),
            new KnowledgeSnippet(
                    "50-30-20 budgeting rule",
                    Set.of("budget", "plan", "income", "spending", "save", "saving"),
                    "A simple budget split is 50 percent needs, 30 percent wants, and 20 percent savings or debt repayment. Adjust the ratio when income is irregular or goals are urgent."),
            new KnowledgeSnippet(
                    "Zero-based budgeting",
                    Set.of("budget", "control", "overspend", "track", "monthly"),
                    "Zero-based budgeting assigns every rupee a job at the start of the month: bills, food, transport, savings, debt, and planned fun. This reduces unplanned leakage."),
            new KnowledgeSnippet(
                    "High-interest debt priority",
                    Set.of("debt", "loan", "credit", "card", "interest", "emi"),
                    "Pay high-interest debt first, especially credit cards and personal loans. Keep minimum payments on all debts, then put extra money toward the highest interest rate."),
            new KnowledgeSnippet(
                    "Subscription and recurring spend audit",
                    Set.of("subscription", "recurring", "netflix", "ott", "gym", "bill", "reduce"),
                    "Audit recurring payments every month. Cancel unused subscriptions, downgrade plans, and move annual renewals into a monthly sinking fund."),
            new KnowledgeSnippet(
                    "Category cap method",
                    Set.of("category", "food", "shopping", "travel", "grocery", "overspend"),
                    "For flexible categories such as food, shopping, travel, and entertainment, set a category cap and review usage weekly instead of waiting until month end."),
            new KnowledgeSnippet(
                    "Goal-based saving",
                    Set.of("goal", "target", "save", "saving", "future", "vacation", "bike", "house"),
                    "For each savings goal, divide the remaining target by the months left. Treat that monthly amount like a bill paid to yourself first."),
            new KnowledgeSnippet(
                    "Sinking fund method",
                    Set.of("annual", "yearly", "insurance", "maintenance", "festival", "school", "fees"),
                    "Use sinking funds for predictable non-monthly costs such as insurance, school fees, festivals, repairs, and travel. Save a small monthly amount before the bill arrives."),
            new KnowledgeSnippet(
                    "Indian tax-saving checklist",
                    Set.of("tax", "section", "80c", "hra", "nps", "elss", "deduction"),
                    "For Indian tax planning, review common deductions such as Section 80C, NPS 80CCD(1B), health insurance under 80D, HRA, home loan interest, and education loan interest. Treat this as educational guidance and verify with a tax professional."),
            new KnowledgeSnippet(
                    "Bank statement reconciliation",
                    Set.of("bank", "statement", "upi", "cash", "card", "notes", "missing"),
                    "When reviewing bank statements, tag unclear UPI/card entries immediately, split mixed purchases into categories, and reconcile cash withdrawals with notes so reports stay accurate."),
            new KnowledgeSnippet(
                    "Cashflow timing",
                    Set.of("cashflow", "salary", "due", "bill", "date", "forecast"),
                    "Compare salary date with bill due dates. Keep fixed bills shortly after income arrives, and avoid large discretionary purchases before essential payments clear."),
            new KnowledgeSnippet(
                    "Savings rate tracking",
                    Set.of("rate", "saving", "progress", "balance", "wealth"),
                    "Track savings rate as savings divided by income. Even a small improvement, such as moving from 10 percent to 15 percent, compounds strongly over time."));

    public String retrieveKnowledge(String query, String financialContext) {
        String searchText = ((query == null ? "" : query) + " " + (financialContext == null ? "" : financialContext))
                .toLowerCase(Locale.ROOT);

        String snippets = KNOWLEDGE_BASE.stream()
                .map(snippet -> new RankedSnippet(snippet, score(snippet, searchText)))
                .filter(ranked -> ranked.score() > 0)
                .sorted(Comparator.comparingInt(RankedSnippet::score).reversed())
                .limit(5)
                .map(ranked -> "- " + ranked.snippet().title() + ": " + ranked.snippet().content())
                .collect(Collectors.joining("\n"));

        if (!snippets.isBlank()) {
            return snippets;
        }

        return KNOWLEDGE_BASE.stream()
                .filter(snippet -> Set.of("Emergency fund rule", "50-30-20 budgeting rule", "Goal-based saving")
                        .contains(snippet.title()))
                .map(snippet -> "- " + snippet.title() + ": " + snippet.content())
                .collect(Collectors.joining("\n"));
    }

    private int score(KnowledgeSnippet snippet, String searchText) {
        int score = 0;
        for (String keyword : snippet.keywords()) {
            if (searchText.contains(keyword)) {
                score++;
            }
        }
        return score;
    }

    private record KnowledgeSnippet(String title, Set<String> keywords, String content) {
    }

    private record RankedSnippet(KnowledgeSnippet snippet, int score) {
    }
}
