import { useState, useEffect } from "react";
import axiosConfig from "../util/axiosConfig.jsx";
import { API_ENDPOINTS } from "../util/apiEndpoints.js";
import { TrendingUp, TrendingDown, Info, Loader2 } from "lucide-react";

const typeConfig = {
    positive: {
        border: "border-l-green-500",
        bg: "bg-green-50",
        icon: TrendingUp,
        iconColor: "text-green-600",
        badge: "bg-green-100 text-green-700",
    },
    warning: {
        border: "border-l-amber-500",
        bg: "bg-amber-50",
        icon: TrendingDown,
        iconColor: "text-amber-600",
        badge: "bg-amber-100 text-amber-700",
    },
    neutral: {
        border: "border-l-blue-500",
        bg: "bg-blue-50",
        icon: Info,
        iconColor: "text-blue-600",
        badge: "bg-blue-100 text-blue-700",
    },
};

const SkeletonCard = () => (
    <div className="animate-pulse flex gap-4 p-4 bg-gray-50 rounded-xl border border-gray-100">
        <div className="w-9 h-9 bg-gray-200 rounded-full flex-shrink-0" />
        <div className="flex-1 space-y-2 pt-1">
            <div className="h-3 bg-gray-200 rounded w-1/2" />
            <div className="h-2 bg-gray-200 rounded w-3/4" />
            <div className="h-2 bg-gray-200 rounded w-2/3" />
        </div>
    </div>
);

const AiInsightsCard = () => {
    const [insights, setInsights] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchInsights = async () => {
            try {
                const response = await axiosConfig.get(API_ENDPOINTS.AI_INSIGHTS);
                setInsights(response.data || []);
            } catch (err) {
                console.error("Failed to fetch AI insights:", err);
                setError("Could not load AI insights right now.");
            } finally {
                setLoading(false);
            }
        };
        fetchInsights();
    }, []);

    return (
        <div className="card col-span-1 md:col-span-2">
            {/* Header */}
            <div className="flex items-center justify-between mb-5">
                <div>
                    <h5 className="text-lg font-semibold flex items-center gap-2">
                        <span className="text-xl">🤖</span> AI Spending Insights
                    </h5>
                    <p className="text-xs text-gray-400 mt-0.5">
                        Personalized analysis of your finances this month
                    </p>
                </div>
                <span className="text-xs font-medium px-2.5 py-1 bg-purple-100 text-purple-700 rounded-full">
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
                <div className="text-center py-6 text-gray-400 text-sm">
                    <p>{error}</p>
                </div>
            )}

            {!loading && !error && insights.length === 0 && (
                <p className="text-sm text-gray-400 text-center py-4">
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
                                className={`flex gap-3 p-4 rounded-xl border-l-4 ${config.border} ${config.bg} transition-all hover:shadow-sm`}
                            >
                                <div className={`w-9 h-9 flex items-center justify-center rounded-full bg-white flex-shrink-0 ${config.iconColor}`}>
                                    <Icon size={17} />
                                </div>
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-start justify-between gap-2">
                                        <p className="text-sm font-semibold text-gray-800 leading-snug">
                                            {insight.title}
                                        </p>
                                        {insight.amount != null && (
                                            <span className={`text-xs font-bold px-2 py-0.5 rounded-full whitespace-nowrap ${config.badge}`}>
                                                ₹{insight.amount.toLocaleString('en-IN')}
                                            </span>
                                        )}
                                    </div>
                                    <p className="text-xs text-gray-500 mt-1 leading-relaxed">
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
