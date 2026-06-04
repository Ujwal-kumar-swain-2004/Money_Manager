package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.ExpenseDTO;
import in.bushansirgur.moneymanager.dto.FinancialInsightsResponse;
import in.bushansirgur.moneymanager.dto.IncomeDTO;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiService {

        private final ChatClient chatClient;

        @Autowired
        private IncomeService incomeService;

        @Autowired
        private ExpenseService expenseService;

        @Autowired
        private ProfileService profileService;

        public AiService(ObjectProvider<ChatClient.Builder> builderProvider) {
                ChatClient.Builder builder = builderProvider.getIfAvailable();
                this.chatClient = builder == null ? null : builder
                                .defaultSystem("""
                                                You are a friendly personal finance advisor for an Indian user.
                                                You ONLY answer questions about personal finance, budgeting, saving, and spending analysis.
                                                Always refer to money amounts in Indian Rupees (₹).
                                                Be concise, warm, and specific. Use numbers from the provided data.
                                                Keep normal chat answers under 180 words unless the user asks for a detailed plan.
                                                If asked about anything unrelated to finance, politely redirect to financial topics.
                                                """)
                                .build();
        }

        /**
         * Feature 1: AI Financial Advisor — answers user's finance question with full
         * context.
         */
        public String getFinancialAdvice(String userQuestion) {
                ProfileEntity profile = profileService.getCurrentProfile();
                String context = buildFinancialContext(profile);

                try {
                        if (chatClient == null) {
                                return buildFallbackAdvice(userQuestion);
                        }
                        return chatClient.prompt()
                                .user(u -> u.text("""
                                                Here is the financial data for the user:
                                                {context}

                                                User's question: {question}

                                                Please answer based on the above data. Keep the answer practical and short.
                                                """)
                                                .param("context", context)
                                                .param("question", userQuestion))
                                .call()
                                .content();
                } catch (Exception ex) {
                        log.warn("AI advice unavailable, using local fallback: {}", ex.getMessage());
                        return buildFallbackAdvice(userQuestion);
                }
        }

        public List<FinancialInsightsResponse.FinancialInsight> getSpendingInsights() {
                ProfileEntity profile = profileService.getCurrentProfile();
                String context = buildFinancialContext(profile);

                try {
                        if (chatClient == null) {
                                return buildFallbackInsights();
                        }
                        FinancialInsightsResponse response = chatClient.prompt()
                                .user(u -> u.text(
                                                """
                                                                Analyze the following financial data and provide 3 to 5 actionable insights.
                                                                Each insight should be specific, using actual numbers from the data.

                                                                Financial Data:
                                                                {context}

                                                                Generate insights covering:
                                                                - Spending patterns and trends
                                                                - Balance health (positive or concerning)
                                                                - Top spending categories
                                                                - Saving opportunities
                                                                - Any anomalies or notable patterns
                                                                """)
                                                .param("context", context))
                                .call()
                                .entity(FinancialInsightsResponse.class);

                        if (response == null || response.getInsights() == null) {
                                return buildFallbackInsights();
                        }
                        return response.getInsights();
                } catch (Exception ex) {
                        log.warn("AI insights unavailable, using local fallback: {}", ex.getMessage());
                        return buildFallbackInsights();
                }
        }

        private String buildFallbackAdvice(String userQuestion) {
                List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
                List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
                BigDecimal totalIncome = totalIncome(incomes);
                BigDecimal totalExpense = totalExpense(expenses);
                BigDecimal savings = totalIncome.subtract(totalExpense);
                Map<String, BigDecimal> expenseByCategory = expenseByCategory(expenses);
                String topCategory = topCategory(expenseByCategory);
                BigDecimal topAmount = expenseByCategory.getOrDefault(topCategory, BigDecimal.ZERO);

                String lowerQuestion = userQuestion == null ? "" : userQuestion.toLowerCase(Locale.ROOT);
                if (lowerQuestion.contains("reduce") || lowerQuestion.contains("save") || lowerQuestion.contains("spending")) {
                        return String.format("""
                                        AI quota is unavailable right now, so here is a quick local analysis from your data.

                                        This month you earned ₹%s and spent ₹%s, leaving ₹%s. Your biggest spend area is %s at ₹%s. Start by setting a 10%% cut target there, which can save around ₹%s. Also review smaller flexible spends like food, shopping, travel, and subscriptions before touching fixed bills.
                                        """,
                                        totalIncome, totalExpense, savings, topCategory, topAmount,
                                        topAmount.multiply(new BigDecimal("0.10")).setScale(0, java.math.RoundingMode.HALF_UP));
                }

                return String.format("""
                                AI quota is unavailable right now, so here is a quick local summary.

                                This month: income ₹%s, expenses ₹%s, net balance ₹%s. Your top expense category is %s at ₹%s. Keep fixed bills planned, and focus on reducing flexible categories first.
                                """,
                                totalIncome, totalExpense, savings, topCategory, topAmount);
        }

        private List<FinancialInsightsResponse.FinancialInsight> buildFallbackInsights() {
                List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
                List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
                BigDecimal income = totalIncome(incomes);
                BigDecimal expense = totalExpense(expenses);
                BigDecimal balance = income.subtract(expense);
                Map<String, BigDecimal> byCategory = expenseByCategory(expenses);
                String topCategory = topCategory(byCategory);
                BigDecimal topAmount = byCategory.getOrDefault(topCategory, BigDecimal.ZERO);

                return List.of(
                                new FinancialInsightsResponse.FinancialInsight(
                                                balance.compareTo(BigDecimal.ZERO) >= 0 ? "Positive balance" : "Balance needs attention",
                                                String.format("Income is ₹%s and expenses are ₹%s, leaving ₹%s this month.", income, expense, balance),
                                                balance.compareTo(BigDecimal.ZERO) >= 0 ? "positive" : "warning",
                                                balance.doubleValue()),
                                new FinancialInsightsResponse.FinancialInsight(
                                                "Top spend category",
                                                String.format("%s is your biggest expense area this month at ₹%s.", topCategory, topAmount),
                                                "warning",
                                                topAmount.doubleValue()),
                                new FinancialInsightsResponse.FinancialInsight(
                                                "Savings opportunity",
                                                String.format("A 10%% cut in %s can save around ₹%s this month.", topCategory,
                                                                topAmount.multiply(new BigDecimal("0.10")).setScale(0, java.math.RoundingMode.HALF_UP)),
                                                "neutral",
                                                topAmount.multiply(new BigDecimal("0.10")).doubleValue()));
        }

        private BigDecimal totalIncome(List<IncomeDTO> incomes) {
                return incomes.stream().map(IncomeDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal totalExpense(List<ExpenseDTO> expenses) {
                return expenses.stream().map(ExpenseDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private Map<String, BigDecimal> expenseByCategory(List<ExpenseDTO> expenses) {
                return expenses.stream()
                                .collect(Collectors.groupingBy(
                                                e -> e.getCategoryName() != null ? e.getCategoryName() : "Uncategorized",
                                                Collectors.reducing(BigDecimal.ZERO, ExpenseDTO::getAmount, BigDecimal::add)));
        }

        private String topCategory(Map<String, BigDecimal> byCategory) {
                return byCategory.entrySet().stream()
                                .max(Comparator.comparing(Map.Entry::getValue))
                                .map(Map.Entry::getKey)
                                .orElse("No expenses yet");
        }

        /**
         * Feature 3: Monthly Report — generates full HTML AI report for a specific
         * user.
         * Called by MonthlyAiReportService scheduler.
         */
        public String generateMonthlyReportHtml(ProfileEntity profile, List<IncomeDTO> incomes,
                        List<ExpenseDTO> expenses) {
                String month = LocalDate.now().minusMonths(1)
                                .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                String year = String.valueOf(LocalDate.now().minusMonths(1).getYear());

                BigDecimal totalIncome = incomes.stream()
                                .map(IncomeDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalExpense = expenses.stream()
                                .map(ExpenseDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Group expenses by category
                Map<String, BigDecimal> byCategory = expenses.stream()
                                .collect(Collectors.groupingBy(
                                                e -> e.getCategoryName() != null ? e.getCategoryName()
                                                                : "Uncategorized",
                                                Collectors.reducing(BigDecimal.ZERO, ExpenseDTO::getAmount,
                                                                BigDecimal::add)));

                String summary = String.format(
                                "User: %s | Month: %s %s | Total Income: ₹%s | Total Expenses: ₹%s | Net Savings: ₹%s | Category Breakdown: %s",
                                profile.getFullName(), month, year,
                                totalIncome, totalExpense,
                                totalIncome.subtract(totalExpense),
                                byCategory);

                if (chatClient == null) {
                        String fallbackNarrative = String.format(
                                        "<p>Your %s %s financial report is ready. Total income was Rs %s, total expenses were Rs %s, and net savings were Rs %s.</p><p>Category breakdown: %s. Review flexible spending first and keep upcoming bills planned before the next month starts.</p>",
                                        month, year, totalIncome, totalExpense, totalIncome.subtract(totalExpense), byCategory);
                        return buildEmailHtmlTemplate(profile.getFullName(), month, year, fallbackNarrative,
                                        totalIncome, totalExpense, totalIncome.subtract(totalExpense));
                }

                String aiNarrative = chatClient.prompt()
                                .user(u -> u.text(
                                                """
                                                                Write a warm, encouraging monthly financial summary email body for {name}.
                                                                Data: {summary}

                                                                Include:
                                                                1. A brief overall assessment (2-3 sentences)
                                                                2. Top 2-3 spending observations with specific amounts
                                                                3. One actionable saving tip for next month
                                                                4. An encouraging closing line

                                                                Format as clean HTML paragraphs. Use ₹ for amounts. Keep it under 200 words.
                                                                Do NOT include html/body/head tags — just the content paragraphs.
                                                                """)
                                                .param("name", profile.getFullName())
                                                .param("summary", summary))
                                .call()
                                .content();
                return buildEmailHtmlTemplate(profile.getFullName(), month, year, aiNarrative,
                                totalIncome, totalExpense, totalIncome.subtract(totalExpense));
        }

        private String buildFinancialContext(ProfileEntity profile) {
                List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
                List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();

                BigDecimal totalIncome = incomes.stream()
                                .map(IncomeDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalExpense = expenses.stream()
                                .map(ExpenseDTO::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                String currentMonth = LocalDate.now().getMonth()
                                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

                Map<String, BigDecimal> expenseByCategory = expenses.stream()
                                .collect(Collectors.groupingBy(
                                                e -> e.getCategoryName() != null ? e.getCategoryName()
                                                                : "Uncategorized",
                                                Collectors.reducing(BigDecimal.ZERO, ExpenseDTO::getAmount,
                                                                BigDecimal::add)));

                Map<String, BigDecimal> incomeByCategory = incomes.stream()
                                .collect(Collectors.groupingBy(
                                                i -> i.getCategoryName() != null ? i.getCategoryName() : "General",
                                                Collectors.reducing(BigDecimal.ZERO, IncomeDTO::getAmount,
                                                                BigDecimal::add)));

                StringBuilder sb = new StringBuilder();
                sb.append("User Name: ").append(profile.getFullName()).append("\n");
                sb.append("Current Month: ").append(currentMonth).append(" ").append(LocalDate.now().getYear())
                                .append("\n\n");
                sb.append("=== INCOME (").append(currentMonth).append(") ===\n");
                sb.append("Total Income: ₹").append(totalIncome).append("\n");
                sb.append("Income Sources: ").append(incomeByCategory).append("\n");
                sb.append("Number of income entries: ").append(incomes.size()).append("\n\n");
                sb.append("=== EXPENSES (").append(currentMonth).append(") ===\n");
                sb.append("Total Expenses: ₹").append(totalExpense).append("\n");
                sb.append("Expense by Category: ").append(expenseByCategory).append("\n");
                sb.append("Number of expense entries: ").append(expenses.size()).append("\n\n");
                sb.append("=== BALANCE ===\n");
                sb.append("Net Balance: ₹").append(totalIncome.subtract(totalExpense)).append("\n");

                if (!incomes.isEmpty()) {
                        sb.append("\n=== INCOME DETAILS ===\n");
                        incomes.stream().limit(15).forEach(i -> sb.append("- ").append(i.getName())
                                        .append(" | ₹").append(i.getAmount())
                                        .append(" | ").append(i.getDate())
                                        .append(" | Category: ").append(i.getCategoryName()).append("\n"));
                }

                if (!expenses.isEmpty()) {
                        sb.append("\n=== EXPENSE DETAILS ===\n");
                        expenses.stream().limit(20).forEach(e -> sb.append("- ").append(e.getName())
                                        .append(" | ₹").append(e.getAmount())
                                        .append(" | ").append(e.getDate())
                                        .append(" | Category: ").append(e.getCategoryName()).append("\n"));
                }

                return sb.toString();
        }

        private String buildEmailHtmlTemplate(String name, String month, String year,
                        String aiContent, BigDecimal income,
                        BigDecimal expense, BigDecimal savings) {
                String savingsColor = savings.compareTo(BigDecimal.ZERO) >= 0 ? "#16a34a" : "#dc2626";
                return String.format(
                                """
                                                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
                                                  <div style="background:linear-gradient(135deg,#581c87,#7c3aed);padding:30px;border-radius:12px 12px 0 0;text-align:center;">
                                                    <h1 style="color:white;margin:0;font-size:24px;">💰 Money Manager</h1>
                                                    <p style="color:#e9d5ff;margin:8px 0 0;">Your %s %s Financial Report</p>
                                                  </div>
                                                  <div style="background:white;padding:30px;border:1px solid #e9d5ff;">
                                                    <h2 style="color:#581c87;">Hi %s! 👋</h2>
                                                    %s
                                                  </div>
                                                  <div style="display:flex;gap:10px;background:#faf5ff;padding:20px;border:1px solid #e9d5ff;border-top:none;">
                                                    <div style="flex:1;text-align:center;padding:15px;background:white;border-radius:8px;border:1px solid #e9d5ff;">
                                                      <p style="margin:0;color:#666;font-size:12px;">Total Income</p>
                                                      <p style="margin:4px 0 0;color:#16a34a;font-size:20px;font-weight:bold;">₹%s</p>
                                                    </div>
                                                    <div style="flex:1;text-align:center;padding:15px;background:white;border-radius:8px;border:1px solid #e9d5ff;">
                                                      <p style="margin:0;color:#666;font-size:12px;">Total Expenses</p>
                                                      <p style="margin:4px 0 0;color:#dc2626;font-size:20px;font-weight:bold;">₹%s</p>
                                                    </div>
                                                    <div style="flex:1;text-align:center;padding:15px;background:white;border-radius:8px;border:1px solid #e9d5ff;">
                                                      <p style="margin:0;color:#666;font-size:12px;">Net Savings</p>
                                                      <p style="margin:4px 0 0;color:%s;font-size:20px;font-weight:bold;">₹%s</p>
                                                    </div>
                                                  </div>
                                                  <div style="background:#faf5ff;padding:15px 20px 20px;border:1px solid #e9d5ff;border-top:none;border-radius:0 0 12px 12px;text-align:center;">
                                                    <a href="${FRONTEND_URL}" style="display:inline-block;padding:12px 24px;background:#7c3aed;color:white;text-decoration:none;border-radius:8px;font-weight:bold;">
                                                      View Full Dashboard →
                                                    </a>
                                                    <p style="color:#999;font-size:11px;margin-top:15px;">Money Manager · AI-Powered Finance Tracking</p>
                                                  </div>
                                                </div>
                                                """,
                                month, year, name, aiContent, income, expense, savingsColor, savings);
        }
}
