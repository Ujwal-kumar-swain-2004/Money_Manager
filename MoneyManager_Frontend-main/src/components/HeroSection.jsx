import {ArrowRight, BadgeIndianRupee, CalendarDays, PieChart} from "lucide-react";
import {Link} from "react-router-dom";

const HeroSection = () => {
    return (
        <section className="bg-[#101914] text-white">
            <div className="mx-auto grid min-h-[calc(100vh-64px)] max-w-7xl items-center gap-10 px-4 py-10 lg:grid-cols-[0.95fr_1.05fr]">
                <div>
                    <p className="text-sm font-semibold uppercase text-[#d9ff72]">Personal finance OS</p>
                    <h1 className="mt-4 max-w-3xl text-4xl font-semibold leading-tight md:text-6xl">
                        Money Manager
                    </h1>
                    <p className="mt-5 max-w-2xl text-lg text-white/65">
                        Track income, expenses, budgets, recurring money, savings goals, and AI guidance from one focused workspace.
                    </p>
                    <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                    <Link
                        to="/signup"
                        className="inline-flex items-center justify-center rounded-md bg-[#d9ff72] px-6 py-3 font-semibold text-[#101914] transition-all hover:bg-[#c9f35b]"
                    >
                        Start Tracking for Free
                    </Link>
                    <Link
                        to="/login"
                        className="inline-flex items-center justify-center gap-2 rounded-md border border-white/15 px-6 py-3 font-semibold text-white hover:bg-white/10"
                    >
                        Open App <ArrowRight className="h-5 w-5" />
                    </Link>
                    </div>
                </div>
                <div className="rounded-lg border border-white/10 bg-white/8 p-4">
                    <div className="rounded-lg border border-white/10 bg-[#0d1711] p-5 text-white">
                        <div className="mb-5 flex items-center justify-between">
                            <div>
                                <p className="text-xs font-semibold uppercase text-white/45">June overview</p>
                                <p className="text-2xl font-semibold">₹84,250</p>
                            </div>
                            <span className="rounded-md bg-[#d9ff72] px-3 py-1 text-xs font-semibold">Healthy</span>
                        </div>
                        <div className="grid gap-3 sm:grid-cols-3">
                            <HeroMetric icon={BadgeIndianRupee} label="Income" value="₹1.2L" />
                            <HeroMetric icon={PieChart} label="Expense" value="₹39K" />
                            <HeroMetric icon={CalendarDays} label="Saved" value="₹42K" />
                        </div>
                        <div className="mt-5 space-y-3">
                            {["Food budget at 62%", "Rent recurring tomorrow", "Emergency goal at 74%"].map((item) => (
                                <div key={item} className="rounded-md border border-white/10 bg-white/[0.04] px-4 py-3 text-sm font-medium text-white/80">{item}</div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

const HeroMetric = ({icon: Icon, label, value}) => (
    <div className="rounded-md border border-white/10 bg-white/[0.04] p-4">
        <Icon size={18} className="text-[#31572c]" />
        <p className="mt-3 text-xs uppercase text-white/45">{label}</p>
        <p className="font-semibold">{value}</p>
    </div>
);

export default HeroSection;
