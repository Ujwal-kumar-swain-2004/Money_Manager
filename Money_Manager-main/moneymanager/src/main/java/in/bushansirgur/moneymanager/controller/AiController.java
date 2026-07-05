package in.bushansirgur.moneymanager.controller;

import in.bushansirgur.moneymanager.dto.AiChatMessageDTO;
import in.bushansirgur.moneymanager.dto.FinancialInsightsResponse;
import in.bushansirgur.moneymanager.entity.ProfileEntity;
import in.bushansirgur.moneymanager.service.AiConversationMemoryService;
import in.bushansirgur.moneymanager.service.AiService;
import in.bushansirgur.moneymanager.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AiConversationMemoryService aiConversationMemoryService;

    /**
     * Feature 1: AI Financial Advisor
     * GET /api/v1.0/ai/advice?question=How+can+I+save+more+this+month
     */
    @GetMapping("/advice")
    public ResponseEntity<Map<String, String>> getFinancialAdvice(
            @RequestParam String question) {

        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Question cannot be empty"));
        }

        String advice = aiService.getFinancialAdvice(question);
        return ResponseEntity.ok(Map.of("answer", advice));
    }

    /**
     * Feature 2: AI Spending Insights
     * GET /api/v1.0/ai/insights
     */
    @GetMapping("/insights")
    public ResponseEntity<List<FinancialInsightsResponse.FinancialInsight>> getSpendingInsights() {
        List<FinancialInsightsResponse.FinancialInsight> insights = aiService.getSpendingInsights();
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AiChatMessageDTO>> getConversationHistory() {
        return ResponseEntity.ok(aiConversationMemoryService.historyForCurrentProfile());
    }

    @DeleteMapping("/memory")
    public ResponseEntity<Map<String, String>> clearConversationMemory() {
        ProfileEntity profile = profileService.getCurrentProfile();
        aiConversationMemoryService.clear(profile.getId());
        return ResponseEntity.ok(Map.of("message", "AI conversation memory cleared"));
    }
}
