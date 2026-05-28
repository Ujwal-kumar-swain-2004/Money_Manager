import { useState, useRef, useEffect } from "react";
import Dashboard from "../components/Dashboard.jsx";
import { useUser } from "../hooks/useUser.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import { API_ENDPOINTS } from "../util/apiEndpoints.js";
import { Bot, Send, User, Loader2, Sparkles } from "lucide-react";
import toast from "react-hot-toast";

const SUGGESTED_QUESTIONS = [
    "How can I reduce my spending this month?",
    "Which category am I spending the most on?",
    "Give me a savings plan based on my income.",
    "Am I on track with my finances?",
    "What are my biggest unnecessary expenses?",
];

const TypingIndicator = () => (
    <div className="flex items-end gap-3 mb-4">
        <div className="w-8 h-8 rounded-full bg-purple-800 flex items-center justify-center flex-shrink-0">
            <Bot size={16} className="text-white" />
        </div>
        <div className="bg-white border border-gray-100 rounded-2xl rounded-bl-none px-4 py-3 shadow-sm">
            <div className="flex gap-1 items-center h-4">
                <span className="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style={{ animationDelay: "0ms" }} />
                <span className="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style={{ animationDelay: "150ms" }} />
                <span className="w-2 h-2 bg-purple-400 rounded-full animate-bounce" style={{ animationDelay: "300ms" }} />
            </div>
        </div>
    </div>
);

const AiAdvisor = () => {
    useUser();
    const [messages, setMessages] = useState([
        {
            role: "assistant",
            text: "Hi! I'm your AI Financial Advisor 👋\n\nI have access to your actual income and expense data, so I can give you personalized financial advice. What would you like to know?",
        },
    ]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const bottomRef = useRef(null);
    const inputRef = useRef(null);

    // Auto-scroll to bottom on new message
    useEffect(() => {
        bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, loading]);

    const sendMessage = async (question) => {
        const q = question || input.trim();
        if (!q || loading) return;

        const userMsg = { role: "user", text: q };
        setMessages(prev => [...prev, userMsg]);
        setInput("");
        setLoading(true);

        try {
            const response = await axiosConfig.get(API_ENDPOINTS.AI_ADVICE, {
                params: { question: q },
            });
            const aiMsg = { role: "assistant", text: response.data.answer };
            setMessages(prev => [...prev, aiMsg]);
        } catch (err) {
            console.error("AI advice error:", err);
            toast.error("Could not get a response. Please try again.");
            setMessages(prev => [...prev, {
                role: "assistant",
                text: "Sorry, I couldn't process that right now. Please try again in a moment.",
            }]);
        } finally {
            setLoading(false);
            inputRef.current?.focus();
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    };

    return (
        <Dashboard activeMenu="AI Advisor">
            <div className="my-5 mx-auto flex flex-col gap-4" style={{ height: "calc(100vh - 140px)" }}>

                {/* Header Card */}
                <div className="card flex items-center gap-4 py-4">
                    <div className="w-12 h-12 bg-gradient-to-br from-purple-600 to-purple-900 rounded-2xl flex items-center justify-center flex-shrink-0 shadow-lg shadow-purple-200">
                        <Bot size={24} className="text-white" />
                    </div>
                    <div>
                        <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                            AI Financial Advisor
                            <span className="text-xs font-medium px-2 py-0.5 bg-purple-100 text-purple-700 rounded-full">
                                <Sparkles size={10} className="inline mr-1" />Powered by AI
                            </span>
                        </h2>
                        <p className="text-sm text-gray-400 mt-0.5">
                            Ask anything about your finances — I have access to your real data
                        </p>
                    </div>
                </div>

                {/* Chat Area */}
                <div className="card flex-1 flex flex-col overflow-hidden p-0">

                    {/* Messages */}
                    <div className="flex-1 overflow-y-auto p-6 space-y-1">
                        {messages.map((msg, i) => (
                            <div
                                key={i}
                                className={`flex items-end gap-3 mb-4 ${msg.role === "user" ? "flex-row-reverse" : ""}`}
                            >
                                {/* Avatar */}
                                <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${
                                    msg.role === "assistant"
                                        ? "bg-purple-800"
                                        : "bg-gray-200"
                                }`}>
                                    {msg.role === "assistant"
                                        ? <Bot size={16} className="text-white" />
                                        : <User size={16} className="text-gray-600" />
                                    }
                                </div>

                                {/* Bubble */}
                                <div className={`max-w-[75%] px-4 py-3 rounded-2xl shadow-sm text-sm leading-relaxed whitespace-pre-wrap ${
                                    msg.role === "assistant"
                                        ? "bg-white border border-gray-100 text-gray-800 rounded-bl-none"
                                        : "bg-purple-800 text-white rounded-br-none"
                                }`}>
                                    {msg.text}
                                </div>
                            </div>
                        ))}

                        {loading && <TypingIndicator />}
                        <div ref={bottomRef} />
                    </div>

                    {/* Suggested Questions (only when no user messages yet) */}
                    {messages.length === 1 && !loading && (
                        <div className="px-6 pb-3">
                            <p className="text-xs text-gray-400 mb-2 font-medium">Suggested questions</p>
                            <div className="flex flex-wrap gap-2">
                                {SUGGESTED_QUESTIONS.map((q, i) => (
                                    <button
                                        key={i}
                                        onClick={() => sendMessage(q)}
                                        className="text-xs px-3 py-1.5 bg-purple-50 text-purple-700 border border-purple-100 rounded-full hover:bg-purple-100 transition-colors cursor-pointer"
                                    >
                                        {q}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Input Bar */}
                    <div className="border-t border-gray-100 p-4 flex items-end gap-3 bg-gray-50/50">
                        <textarea
                            ref={inputRef}
                            value={input}
                            onChange={e => setInput(e.target.value)}
                            onKeyDown={handleKeyDown}
                            placeholder="Ask about your finances... (Enter to send)"
                            rows={1}
                            className="flex-1 resize-none border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-purple-400 focus:ring-2 focus:ring-purple-100 bg-white transition-all"
                            style={{ maxHeight: "120px" }}
                            disabled={loading}
                        />
                        <button
                            onClick={() => sendMessage()}
                            disabled={!input.trim() || loading}
                            className={`w-10 h-10 flex items-center justify-center rounded-xl transition-all flex-shrink-0 ${
                                input.trim() && !loading
                                    ? "bg-purple-800 text-white hover:bg-purple-700 shadow-md shadow-purple-200 cursor-pointer"
                                    : "bg-gray-200 text-gray-400 cursor-not-allowed"
                            }`}
                        >
                            {loading
                                ? <Loader2 size={16} className="animate-spin" />
                                : <Send size={16} />
                            }
                        </button>
                    </div>
                </div>
            </div>
        </Dashboard>
    );
};

export default AiAdvisor;
