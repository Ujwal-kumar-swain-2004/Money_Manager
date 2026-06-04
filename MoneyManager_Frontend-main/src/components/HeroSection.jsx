import {
    ArrowRight,
    BadgeIndianRupee,
    Bell,
    Bot,
    CalendarDays,
    CheckCircle2,
    CreditCard,
    HandCoins,
    PieChart,
    ShieldCheck,
    UsersRound
} from "lucide-react";
import {Link} from "react-router-dom";

const HeroSection = () => {
    return (
        <section className="relative overflow-hidden bg-[#f7f8f3] text-slate-950">
            <div className="mx-auto grid min-h-[calc(100vh-64px)] max-w-7xl items-center gap-10 px-4 py-10 lg:grid-cols-[0.9fr_1.1fr]">
                <div className="max-w-2xl">
                    <p className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-3 py-1 text-sm font-semibold text-slate-700">
                        <ShieldCheck size={16} className="text-emerald-600" />
                        Personal, family, and friends money in one app
                    </p>
                    <h1 className="mt-5 text-4xl font-bold leading-tight text-slate-950 md:text-6xl">
                        Money Manager
                    </h1>
                    <p className="mt-5 text-lg leading-8 text-slate-600">
                        Track your own spending, manage family allowances, split bills with friends, plan budgets, and get smart insights without jumping between different apps.
                    </p>
                    <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                        <Link
                            to="/signup"
                            className="inline-flex items-center justify-center rounded-md bg-slate-950 px-6 py-3 font-semibold text-white transition hover:bg-slate-800"
                        >
                            Start Free
                        </Link>
                        <Link
                            to="/login"
                            className="inline-flex items-center justify-center gap-2 rounded-md border border-slate-300 bg-white px-6 py-3 font-semibold text-slate-950 transition hover:border-slate-950"
                        >
                            Open Demo <ArrowRight className="h-5 w-5" />
                        </Link>
                    </div>
                    <div className="mt-8 grid gap-3 text-sm text-slate-700 sm:grid-cols-3">
                        <TrustItem text="Budget alerts" />
                        <TrustItem text="Split expenses" />
                        <TrustItem text="AI insights" />
                    </div>
                </div>

                <div className="relative">
                    <div className="rounded-[28px] border border-slate-200 bg-white p-3 shadow-2xl shadow-slate-300/70">
                        <div className="rounded-[22px] bg-slate-950 p-4 text-white md:p-5">
                            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-white/10 pb-4">
                                <div>
                                    <p className="text-xs font-semibold uppercase tracking-wide text-[#d9ff72]">June workspace</p>
                                    <h2 className="mt-1 text-2xl font-semibold">Rs 84,250 balance</h2>
                                </div>
                                <span className="rounded-full bg-[#d9ff72] px-3 py-1 text-xs font-bold text-slate-950">Healthy month</span>
                            </div>

                            <div className="mt-4 grid gap-3 md:grid-cols-3">
                                <HeroMetric icon={BadgeIndianRupee} label="Income" value="Rs 1.22L" tone="bg-emerald-400" />
                                <HeroMetric icon={PieChart} label="Spent" value="Rs 39K" tone="bg-rose-300" />
                                <HeroMetric icon={CalendarDays} label="Planned" value="9 bills" tone="bg-sky-300" />
                            </div>

                            <div className="mt-4 grid gap-3 lg:grid-cols-[1.05fr_0.95fr]">
                                <div className="rounded-2xl border border-white/10 bg-white/[0.06] p-4">
                                    <div className="flex items-center justify-between">
                                        <p className="font-semibold">Where did my money go?</p>
                                        <PieChart size={18} className="text-[#d9ff72]" />
                                    </div>
                                    <CategoryBar name="Rent & Bills" value="52%" width="82%" />
                                    <CategoryBar name="Food & Dining" value="18%" width="42%" />
                                    <CategoryBar name="Shopping" value="14%" width="34%" />
                                </div>
                                <div className="grid gap-3">
                                    <FeaturePill icon={HandCoins} title="Rahul owes you" value="Rs 740" />
                                    <FeaturePill icon={UsersRound} title="Family allowance" value="Rs 12,000" />
                                    <FeaturePill icon={Bot} title="AI tip ready" value="Save Rs 3,200" />
                                </div>
                            </div>

                            <div className="mt-4 grid gap-3 md:grid-cols-3">
                                <MiniCard icon={Bell} title="Rent due" text="Tomorrow" />
                                <MiniCard icon={CreditCard} title="UPI tracked" text="42 payments" />
                                <MiniCard icon={CheckCircle2} title="Goal progress" text="74%" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    );
};

const TrustItem = ({text}) => (
    <div className="flex items-center gap-2 rounded-md border border-slate-200 bg-white px-3 py-2">
        <CheckCircle2 size={16} className="text-emerald-600" />
        <span className="font-semibold">{text}</span>
    </div>
);

const HeroMetric = ({icon: Icon, label, value, tone}) => (
    <div className="rounded-2xl border border-white/10 bg-white/[0.06] p-4">
        <span className={`inline-flex h-9 w-9 items-center justify-center rounded-md ${tone} text-slate-950`}>
            <Icon size={18} />
        </span>
        <p className="mt-3 text-xs uppercase text-white/55">{label}</p>
        <p className="text-xl font-semibold">{value}</p>
    </div>
);

const CategoryBar = ({name, value, width}) => (
    <div className="mt-4">
        <div className="mb-2 flex items-center justify-between text-sm">
            <span className="text-white/74">{name}</span>
            <span className="font-semibold">{value}</span>
        </div>
        <div className="h-2 rounded-full bg-white/10">
            <div className="h-2 rounded-full bg-[#d9ff72]" style={{width}} />
        </div>
    </div>
);

const FeaturePill = ({icon: Icon, title, value}) => (
    <div className="rounded-2xl border border-white/10 bg-white/[0.06] p-4">
        <Icon size={18} className="text-[#d9ff72]" />
        <p className="mt-2 text-sm text-white/62">{title}</p>
        <p className="font-semibold">{value}</p>
    </div>
);

const MiniCard = ({icon: Icon, title, text}) => (
    <div className="rounded-2xl border border-white/10 bg-white/[0.06] p-4">
        <Icon size={18} className="text-[#d9ff72]" />
        <p className="mt-2 text-sm font-semibold">{title}</p>
        <p className="text-sm text-white/58">{text}</p>
    </div>
);

export default HeroSection;
