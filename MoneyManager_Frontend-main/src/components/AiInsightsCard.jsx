import axiosConfig from "../util/axiosConfig.jsx";
import { API_ENDPOINTS } from "../util/apiEndpoints.js";
import { TrendingUp, TrendingDown, Info } from "lucide-react";
import {useQuery} from "@tanstack/react-query";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const typeConfig = {
    positive: {
        border: "border-l-green-500",
        bg: "bg-emerald-500/10",
        icon: TrendingUp,
        iconColor: "text-emerald-300",
        badge: "bg-emerald-400/15 text-emerald-200",
    },
    warning: {
        border: "border-l-amber-500",
        bg: "bg-amber-500/10",
        icon: TrendingDown,
        iconColor: "text-amber-300",
        badge: "bg-amber-400/15 text-amber-200",
    },
    neutral: {
        border: "border-l-blue-500",
        bg: "bg-sky-500/10",
        icon: Info,
        iconColor: "text-sky-300",
        badge: "bg-sky-400/15 text-sky-200",
    },
};

const SkeletonCard = () => (
    <div className="animate-pulse flex gap-4 p-4 bg-white/[0.03] rounded-xl border border-white/14">
        <div className="w-9 h-9 bg-white/10 rounded-full flex-shrink-0" />
        <div className="flex-1 space-y-2 pt-1">
            <div className="h-3 bg-white/10 rounded w-1/2" />
            <div className="h-2 bg-white/10 rounded w-3/4" />
            <div className="h-2 bg-white/10 rounded w-2/3" />
        </div>
    </div>
);

const AiInsightsCard = () => {
    const {data: insights = [], isLoading: loading, error} = useQuery({
        queryKey: queryKeys.aiInsights,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.AI_INSIGHTS, {timeout: 60000});
            return response.data || [];
        },
        staleTime: cacheTimes.aiInsights,
        gcTime: cacheTimes.aiInsights,
        retry: false,
    });

    if (error) {
        console.error("Failed to fetch AI insights:", error.response?.data || error.message);
    }

    return (
        <div className="card col-span-1 md:col-span-2">
            {/* Header */}
            <div className="flex items-center justify-between mb-5">
                <div>
                    <h5 className="text-lg font-semibold flex items-center gap-2">
                        <span className="text-xl">🤖</span> AI Spending Insights
                    </h5>
                    <p className="text-xs text-white/65 mt-0.5">
                        Personalized analysis of your finances this month
                    </p>
                </div>
                <span className="text-xs font-medium px-2.5 py-1 bg-[#d9ff72]/15 text-[#d9ff72] rounded-full">
                    Powered by AI
                </span>
            </div>

            {/* Content */}
            {loading && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {[...Array(4)].map((_, i) => <SkeletonCard key={i} />)}
                </div>
            )}

            {error && (
                <div className="text-center py-6 text-white/65 text-sm">
                    <p>Could not load AI insights right now.</p>
                </div>
            )}

            {!loading && !error && insights.length === 0 && (
                <p className="text-sm text-white/65 text-center py-4">
                    Add some income and expenses to get AI insights!
                </p>
            )}

            {!loading && !error && insights.length > 0 && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    {insights.map((insight, index) => {
                        const config = typeConfig[insight.type] || typeConfig.neutral;
                        const Icon = config.icon;
                        return (
                            <div
                                key={index}
                                className={`flex gap-3 p-4 rounded-xl border border-white/14 border-l-4 ${config.border} ${config.bg} transition-all hover:bg-white/[0.06]`}
                            >
                                <div className={`w-9 h-9 flex items-center justify-center rounded-full bg-white/10 flex-shrink-0 ${config.iconColor}`}>
                                    <Icon size={17} />
                                </div>
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-start justify-between gap-2">
                                        <p className="text-sm font-semibold text-white leading-snug">
                                            {insight.title}
                                        </p>
                                        {insight.amount != null && (
                                            <span className={`text-xs font-bold px-2 py-0.5 rounded-full whitespace-nowrap ${config.badge}`}>
                                                ₹{insight.amount.toLocaleString('en-IN')}
                                            </span>
                                        )}
                                    </div>
                                    <p className="text-xs text-white/65 mt-1 leading-relaxed">
                                        {insight.description}
                                    </p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default AiInsightsCard;

