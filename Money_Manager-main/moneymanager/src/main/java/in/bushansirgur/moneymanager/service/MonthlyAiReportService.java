package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.dto.ExpenseDTO;
import in.bushansirgur.moneymanager.dto.IncomeDTO;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Feature 3: Monthly AI Financial Report
 * Runs on the 1st of every month at 9:00 AM IST.
 * Generates a personalized AI-written HTML report for each user
 * covering the previous month's income, expenses, and saving tips.
 */
@Service
public class MonthlyAiReportService {

    private static final Logger log = LoggerFactory.getLogger(MonthlyAiReportService.class);

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private AiService aiService;

    @Autowired
    private EmailService emailService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    /**
     * Cron: every 1st of the month at 9:00 AM IST
     */
    @Scheduled(cron = "0 0 9 1 * *", zone = "Asia/Kolkata")
    public void sendMonthlyAiReport() {
        log.info("Job started: sendMonthlyAiReport()");

        List<ProfileEntity> profiles = profileRepository.findAll();
        LocalDate lastMonth = LocalDate.now().minusMonths(1);

        for (ProfileEntity profile : profiles) {
            try {
                // Fetch last month's data for this specific user
                LocalDate startDate = lastMonth.withDayOfMonth(1);
                LocalDate endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

                List<IncomeDTO> incomes = incomeService
                        .getIncomesForUserInRange(profile.getId(), startDate, endDate);
                List<ExpenseDTO> expenses = expenseService
                        .getExpensesForUserInRange(profile.getId(), startDate, endDate);

                // Skip users with no data last month
                if (incomes.isEmpty() && expenses.isEmpty()) {
                    log.info("Skipping {} — no data for last month", profile.getEmail());
                    continue;
                }

                // Generate AI-written report HTML
                String htmlBody = aiService.generateMonthlyReportHtml(profile, incomes, expenses);

                String subject = String.format("📊 Your %s Financial Report — Money Manager",
                        lastMonth.getMonth().name().charAt(0)
                                + lastMonth.getMonth().name().substring(1).toLowerCase());

                emailService.sendHtmlEmail(profile.getEmail(), subject, htmlBody);
                log.info("Monthly AI report sent to {}", profile.getEmail());

            } catch (Exception e) {
                log.error("Failed to send monthly AI report to {}: {}", profile.getEmail(), e.getMessage());
            }
        }

        log.info("Job completed: sendMonthlyAiReport()");
    }
}
