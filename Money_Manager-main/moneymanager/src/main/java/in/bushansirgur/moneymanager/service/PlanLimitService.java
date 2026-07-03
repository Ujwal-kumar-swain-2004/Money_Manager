package in.bushansirgur.moneymanager.service;

import in.bushansirgur.moneymanager.entity.ProfileEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PlanLimitService {
    public static final int FREE_FRIENDS = 5;
    public static final int FREE_GROUPS = 2;
    public static final int FREE_SHARED_EXPENSES_PER_MONTH = 20;

    public boolean isPro(ProfileEntity profile) {
        return profile != null && "PRO".equalsIgnoreCase(profile.getPlanType());
    }

    public void requirePro(ProfileEntity profile, String featureName) {
        if (!isPro(profile)) {
            throw new RuntimeException("Upgrade to Pro to use " + featureName);
        }
    }

    public void checkFreeLimit(ProfileEntity profile, long currentCount, long maxAllowed, String featureName) {
        if (!isPro(profile) && currentCount >= maxAllowed) {
            throw new RuntimeException("Free plan limit reached for " + featureName + ". Upgrade to Pro for more.");
        }
    }

    public Map<String, Object> friendLimits(ProfileEntity profile, long friends, long groups, long sharedExpensesThisMonth) {
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("planType", isPro(profile) ? "PRO" : "FREE");
        limits.put("friendsModule", true);
        limits.put("liveFriendConnections", isPro(profile));
        limits.put("friendsUsed", friends);
        limits.put("friendsLimit", isPro(profile) ? "Unlimited" : FREE_FRIENDS);
        limits.put("groupsUsed", groups);
        limits.put("groupsLimit", isPro(profile) ? "Unlimited" : FREE_GROUPS);
        limits.put("sharedExpensesThisMonth", sharedExpensesThisMonth);
        limits.put("sharedExpensesLimit", isPro(profile) ? "Unlimited" : FREE_SHARED_EXPENSES_PER_MONTH);
        limits.put("advancedSplits", isPro(profile));
        limits.put("receiptUpload", isPro(profile));
        limits.put("recurringSharedExpenses", isPro(profile));
        limits.put("groupReports", isPro(profile));
        limits.put("aiSettlementSuggestions", isPro(profile));
        return limits;
    }
}
