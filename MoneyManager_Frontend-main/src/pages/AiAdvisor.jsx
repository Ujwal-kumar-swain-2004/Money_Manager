import {useEffect, useRef, useState} from "react";
import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import {Bot, Loader2, Send, Sparkles, Trash2, User} from "lucide-react";
import toast from "react-hot-toast";

const SUGGESTED_QUESTIONS = [
    "How can I reduce my spending this month?",
    "Which category am I spending the most on?",
    "Give me a savings plan based on my income.",
    "Am I on track with my finances?",
    "What are my biggest unnecessary expenses?",
];

const DEFAULT_MESSAGES = [
    {
        role: "assistant",
        text: "Hi, I am your AI Financial Advisor.\n\nI can use your income and expense data to help you understand spending, budgets, savings, and cashflow. What would you like to know?",
    },
];

const AiAdvisor = () => {
    useUser();
    const [messages, setMessages] = useState(DEFAULT_MESSAGES);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const bottomRef = useRef(null);
    const inputRef = useRef(null);

    useEffect(() => {
        bottomRef.current?.scrollIntoView({behavior: "smooth"});
    }, [messages, loading]);

    useEffect(() => {
        const loadHistory = async () => {
            try {
                const response = await axiosConfig.get(API_ENDPOINTS.AI_HISTORY);
                const history = response.data || [];
                setMessages(history.length ? history.map((item) => ({role: item.role, text: item.text})) : DEFAULT_MESSAGES);
            } catch (err) {
                console.error("AI history error:", err.response?.data || err.message);
            }
        };

        loadHistory();
    }, []);

    const sendMessage = async (question) => {
        const q = question || input.trim();
        if (!q || loading) return;

        setMessages((prev) => [...prev, {role: "user", text: q}]);
        setInput("");
        setLoading(true);

        try {
            const response = await axiosConfig.get(API_ENDPOINTS.AI_ADVICE, {
                params: {question: q},
                timeout: 120000,
            });
            setMessages((prev) => [...prev, {role: "assistant", text: response.data.answer}]);
        } catch (err) {
            console.error("AI advice error:", err.response?.data || err.message);
            const timedOut = err.code === "ECONNABORTED";
            toast.error(timedOut ? "AI is taking longer than usual. Please try again." : "Could not get a response. Please try again.");
            setMessages((prev) => [...prev, {
                role: "assistant",
                text: timedOut
                    ? "The local AI model took too long to respond. Ollama may still be loading the model, so try again in a moment."
                    : "Sorry, I could not process that right now. Please try again in a moment.",
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

    const clearHistory = async () => {
        try {
            await axiosConfig.delete(API_ENDPOINTS.AI_MEMORY);
            setMessages(DEFAULT_MESSAGES);
            toast.success("AI chat history cleared");
        } catch (err) {
            console.error("AI clear history error:", err.response?.data || err.message);
            toast.error("Could not clear chat history");
        }
    };

    return (
        <Dashboard activeMenu="AI Advisor">
            <div className="mx-auto flex max-w-7xl flex-col gap-4" style={{height: "calc(100vh - 110px)"}}>
                <div className="flex items-center gap-4 rounded-lg bg-[#1f2a24] p-5 text-white">
                    <div className="flex h-12 w-12 items-center justify-center rounded-md bg-[#d9ff72] text-[#1f2a24]">
                        <Bot size={24} />
                    </div>
                    <div>
                        <h2 className="flex items-center gap-2 text-xl font-semibold">
                            AI Financial Advisor
                            <span className="rounded-md bg-white/10 px-2 py-0.5 text-xs font-medium text-[#d9ff72]">
                                <Sparkles size={10} className="inline mr-1" />
                                Powered by AI
                            </span>
                        </h2>
                        <p className="mt-0.5 text-sm text-white/72">Ask anything about your finances using your real app data.</p>
                    </div>
                    <button
                        type="button"
                        onClick={clearHistory}
                        className="ml-auto flex h-10 w-10 items-center justify-center rounded-md border border-white/14 bg-white/5 text-white/72 transition-colors hover:border-[#d9ff72]/60 hover:text-[#d9ff72]"
                        title="Clear chat history"
                    >
                        <Trash2 size={16} />
                    </button>
                </div>

                <div className="panel flex flex-1 flex-col overflow-hidden p-0">
                    <div className="flex-1 overflow-y-auto p-6">
                        {messages.map((msg, index) => (
                            <div key={index} className={`mb-4 flex items-end gap-3 ${msg.role === "user" ? "flex-row-reverse" : ""}`}>
                                <div className={`flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-md ${msg.role === "assistant" ? "bg-[#31572c]" : "bg-white/10"}`}>
                                    {msg.role === "assistant" ? <Bot size={16} className="text-white" /> : <User size={16} className="text-white/78" />}
                                </div>
                                <div className={`max-w-[75%] rounded-lg px-4 py-3 text-sm leading-relaxed shadow-sm whitespace-pre-wrap ${msg.role === "assistant" ? "border border-white/14 bg-white/[0.04] text-white/80" : "bg-[#1f2a24] text-white"}`}>
                                    {msg.text}
                                </div>
                            </div>
                        ))}

                        {loading && <TypingIndicator />}
                        <div ref={bottomRef} />
                    </div>

                    {messages.length === 1 && !loading && (
                        <div className="px-6 pb-3">
                            <p className="mb-2 text-xs font-semibold uppercase text-white/65">Suggested questions</p>
                            <div className="flex flex-wrap gap-2">
                                {SUGGESTED_QUESTIONS.map((question) => (
                                    <button
                                        key={question}
                                        onClick={() => sendMessage(question)}
                                        className="rounded-md border border-emerald-100 bg-emerald-50 px-3 py-1.5 text-xs font-medium text-emerald-800 transition-colors hover:bg-emerald-100"
                                    >
                                        {question}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="flex items-end gap-3 border-t border-white/14 bg-black/15 p-4">
                        <textarea
                            ref={inputRef}
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={handleKeyDown}
                            placeholder="Ask about your finances... Enter to send"
                            rows={1}
                            className="flex-1 resize-none rounded-md border border-white/14 bg-black/15 px-4 py-2.5 text-sm text-white outline-none transition-all placeholder:text-white/78 focus:border-[#d9ff72]/70 focus:ring-2 focus:ring-[#d9ff72]/10"
                            style={{maxHeight: "120px"}}
                            disabled={loading}
                        />
                        <button
                            onClick={() => sendMessage()}
                            disabled={!input.trim() || loading}
                            className={`flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-md transition-all ${
                                input.trim() && !loading
                                    ? "cursor-pointer bg-[#d9ff72] text-[#1f2a24] hover:bg-[#c9f35b]"
                                    : "cursor-not-allowed bg-white/10 text-white/78"
                            }`}
                        >
                            {loading ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
                        </button>
                    </div>
                </div>
            </div>
        </Dashboard>
    );
};

const TypingIndicator = () => (
    <div className="mb-4 flex items-end gap-3">
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-md bg-[#31572c]">
            <Bot size={16} className="text-white" />
        </div>
        <div className="rounded-lg border border-white/14 bg-white/[0.04] px-4 py-3 shadow-sm">
            <div className="flex h-4 items-center gap-1">
                <span className="h-2 w-2 animate-bounce rounded-full bg-[#31572c]" style={{animationDelay: "0ms"}} />
                <span className="h-2 w-2 animate-bounce rounded-full bg-[#31572c]" style={{animationDelay: "150ms"}} />
                <span className="h-2 w-2 animate-bounce rounded-full bg-[#31572c]" style={{animationDelay: "300ms"}} />
            </div>
        </div>
    </div>
);

export default AiAdvisor;

