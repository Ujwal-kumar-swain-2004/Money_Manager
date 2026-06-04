import {QueryClient} from "@tanstack/react-query";

export const queryKeys = {
    dashboard: ["dashboard"],
    categories: ["categories"],
    categoryType: (type) => ["categories", type],
    incomes: ["incomes"],
    expenses: ["expenses"],
    moneyPlan: (month, year) => ["money-plan", month, year],
    familyDashboard: (familyId) => ["family-dashboard", familyId],
    familyMembers: (familyId) => ["family-members", familyId],
    families: ["families"],
    friendsDashboard: ["friends-dashboard"],
    aiInsights: ["ai-insights"],
};

export const cacheTimes = {
    dashboard: 5 * 60 * 1000,
    categories: 30 * 60 * 1000,
    transactions: 2 * 60 * 1000,
    moneyPlan: 5 * 60 * 1000,
    family: 5 * 60 * 1000,
    friends: 5 * 60 * 1000,
    aiInsights: 12 * 60 * 60 * 1000,
};

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
            retry: 1,
            staleTime: 2 * 60 * 1000,
            gcTime: 30 * 60 * 1000,
        },
    },
});

export default queryClient;
