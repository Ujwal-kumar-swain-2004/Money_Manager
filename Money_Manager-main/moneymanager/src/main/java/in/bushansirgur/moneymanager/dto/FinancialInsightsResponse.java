package in.bushansirgur.moneymanager.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class FinancialInsightsResponse {

        @JsonPropertyDescription("A list of 3 to 5 financial insights about the user's spending and income patterns")
        private List<FinancialInsight> insights;

        // Default Constructor
        public FinancialInsightsResponse() {
        }

        // Parameterized Constructor
        public FinancialInsightsResponse(List<FinancialInsight> insights) {
                this.insights = insights;
        }

        // Getter
        public List<FinancialInsight> getInsights() {
                return insights;
        }

        // Setter
        public void setInsights(List<FinancialInsight> insights) {
                this.insights = insights;
        }

        // Inner Class
        public static class FinancialInsight {

                @JsonPropertyDescription("A short, clear title for this insight (max 8 words)")
                private String title;

                @JsonPropertyDescription("A helpful 1-2 sentence description explaining the insight with specific numbers from the data")
                private String description;

                @JsonPropertyDescription("The type of insight: 'positive' for good news, 'warning' for areas of concern, 'neutral' for informational")
                private String type;

                @JsonPropertyDescription("The relevant monetary amount in rupees, or null if not applicable")
                private Double amount;

                // Default Constructor
                public FinancialInsight() {
                }

                // Parameterized Constructor
                public FinancialInsight(String title, String description, String type, Double amount) {
                        this.title = title;
                        this.description = description;
                        this.type = type;
                        this.amount = amount;
                }

                // Getters and Setters

                public String getTitle() {
                        return title;
                }

                public void setTitle(String title) {
                        this.title = title;
                }

                public String getDescription() {
                        return description;
                }

                public void setDescription(String description) {
                        this.description = description;
                }

                public String getType() {
                        return type;
                }

                public void setType(String type) {
                        this.type = type;
                }

                public Double getAmount() {
                        return amount;
                }

                public void setAmount(Double amount) {
                        this.amount = amount;
                }
        }
}