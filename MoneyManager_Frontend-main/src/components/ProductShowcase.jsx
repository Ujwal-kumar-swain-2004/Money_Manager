import {
    BadgeIndianRupee,
    BellRing,
    Bot,
    CalendarClock,
    ChartNoAxesCombined,
    FileSpreadsheet,
    HandCoins,
    PiggyBank,
    Receipt,
    Repeat,
    ShieldCheck,
    UsersRound,
    WalletCards
} from "lucide-react";
import {Link} from "react-router-dom";

const featureGroups = [
    {
        eyebrow: "Personal finance",
        title: "Know every rupee clearly.",
        text: "Track income, expenses, categories, notes, payment methods, and monthly comparisons from one clean dashboard.",
        icon: BadgeIndianRupee,
        items: ["Income and expense CRUD", "Category breakdown", "Spending calendar", "Excel and email reports"],
    },
    {
        eyebrow: "Money plan",
        title: "Plan before the money leaves.",
        text: "Budgets, recurring transactions, bill reminders, savings goals, and cashflow forecast help the user stay ahead.",
        icon: PiggyBank,
        items: ["Monthly budgets", "Savings contributions", "Recurring automation", "Budget alerts"],
    },
    {
        eyebrow: "Family workspace",
        title: "Manage household money together.",
        text: "Add family members, track allowances, record transfers, and see who spent what inside the household.",
        icon: UsersRound,
        items: ["Family members", "Allowances", "Transfers", "Member-wise spending"],
    },
    {
        eyebrow: "Friends splitting",
        title: "Split bills like a real shared expense app.",
        text: "Create friends and groups, split by equal/exact/percentage/shares, settle up, remind, and comment.",
        icon: HandCoins,
        items: ["Friend balances", "Shared groups", "Settle up", "Activity timeline"],
    },
];

const workflow = [
    {icon: Receipt, title: "Add transactions", text: "Capture income, expenses, notes, tags, and payment method."},
    {icon: WalletCards, title: "Plan money", text: "Set budgets, savings goals, subscriptions, and upcoming bills."},
    {icon: Repeat, title: "Automate repeats", text: "Auto-create monthly salaries, rent, EMIs, and subscriptions."},
    {icon: ChartNoAxesCombined, title: "Read insights", text: "See categories, month comparison, cashflow, and AI guidance."},
];

const ProductShowcase = () => {
    return (
        <div className="bg-white text-slate-950">
            <section id="features" className="border-y border-slate-200 bg-white py-16">
                <div className="mx-auto max-w-7xl px-4">
                    <div className="max-w-3xl">
                        <p className="text-sm font-bold uppercase tracking-wide text-emerald-700">Everything visible from day one</p>
                        <h2 className="mt-3 text-3xl font-bold md:text-5xl">More than a CRUD app. A complete money workspace.</h2>
                        <p className="mt-4 text-lg text-slate-600">
                            The homepage now shows the whole system clearly, so new users understand the value before signing up.
                        </p>
                    </div>

                    <div className="mt-10 grid gap-4 md:grid-cols-2">
                        {featureGroups.map((feature) => (
                            <FeaturePanel key={feature.title} {...feature} />
                        ))}
                    </div>
                </div>
            </section>

            <section id="friends" className="bg-[#f7f8f3] py-16">
                <div className="mx-auto grid max-w-7xl gap-8 px-4 lg:grid-cols-[0.95fr_1.05fr]">
                    <div>
                        <p className="text-sm font-bold uppercase tracking-wide text-emerald-700">Splitwise-style section</p>
                        <h2 className="mt-3 text-3xl font-bold md:text-5xl">Friends, groups, balances, and settlements.</h2>
                        <p className="mt-4 text-lg leading-8 text-slate-600">
                            Add a dinner, trip, rent, or office lunch. Select who paid, split it fairly, and the app shows exactly who owes whom.
                        </p>
                    </div>
                    <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-xl shadow-slate-200">
                        <div className="grid gap-3 md:grid-cols-2">
                            <SplitCard name="Goa Trip" amount="Rs 18,600" meta="4 friends | exact split" status="Rahul owes Rs 2,450" />
                            <SplitCard name="Dinner" amount="Rs 2,800" meta="equal split | UPI" status="You are owed Rs 700" />
                            <SplitCard name="Apartment Bills" amount="Rs 9,200" meta="roommates | shares" status="Settled by bank" />
                            <SplitCard name="Office Lunch" amount="Rs 1,560" meta="percentage split" status="Reminder due today" />
                        </div>
                    </div>
                </div>
            </section>

            <section id="family" className="bg-slate-950 py-16 text-white">
                <div className="mx-auto max-w-7xl px-4">
                    <div className="grid gap-8 lg:grid-cols-[0.9fr_1.1fr]">
                        <div>
                            <p className="text-sm font-bold uppercase tracking-wide text-[#d9ff72]">Family money manager</p>
                            <h2 className="mt-3 text-3xl font-bold md:text-5xl">Designed for real household money flow.</h2>
                            <p className="mt-4 text-lg leading-8 text-white/65">
                                Father gives allowance, son and daughter make purchases, household bills are paid, and everyone stays visible.
                            </p>
                        </div>
                        <div className="grid gap-3 md:grid-cols-3">
                            <FamilyStat label="Father" value="Rs 52,000" text="income added" />
                            <FamilyStat label="Son" value="Rs 4,800" text="spent this month" />
                            <FamilyStat label="Daughter" value="Rs 3,200" text="allowance left" />
                        </div>
                    </div>
                </div>
            </section>

            <section className="bg-white py-16">
                <div className="mx-auto max-w-7xl px-4">
                    <div className="grid gap-4 md:grid-cols-4">
                        {workflow.map((item) => (
                            <WorkflowCard key={item.title} {...item} />
                        ))}
                    </div>
                </div>
            </section>

            <section id="pricing" className="border-t border-slate-200 bg-[#f7f8f3] py-16">
                <div className="mx-auto grid max-w-7xl gap-6 px-4 lg:grid-cols-[0.8fr_1.2fr]">
                    <div>
                        <p className="text-sm font-bold uppercase tracking-wide text-emerald-700">Free now, pro ready later</p>
                        <h2 className="mt-3 text-3xl font-bold md:text-5xl">Start simple. Upgrade when automation matters.</h2>
                    </div>
                    <div className="grid gap-4 md:grid-cols-2">
                        <PlanCard
                            title="Free"
                            price="Rs 0"
                            items={["Personal dashboard", "Basic budgets", "Limited friends", "Limited AI insights"]}
                        />
                        <PlanCard
                            title="Pro"
                            price="Coming later"
                            highlighted
                            items={["Unlimited groups", "AI monthly report", "Family workspace", "Receipts and exports"]}
                        />
                    </div>
                </div>
            </section>

            <section className="bg-slate-950 px-4 py-14 text-center text-white">
                <Bot className="mx-auto text-[#d9ff72]" size={34} />
                <h2 className="mt-4 text-3xl font-bold">Ready to see your money clearly?</h2>
                <p className="mx-auto mt-3 max-w-2xl text-white/62">
                    Use the demo data or create your own workspace for personal, family, and friends transactions.
                </p>
                <div className="mt-7 flex flex-col justify-center gap-3 sm:flex-row">
                    <Link to="/signup" className="rounded-md bg-[#d9ff72] px-6 py-3 font-bold text-slate-950 hover:bg-[#c9f35b]">
                        Create Account
                    </Link>
                    <Link to="/login" className="rounded-md border border-white/20 px-6 py-3 font-bold text-white hover:bg-white/10">
                        Login
                    </Link>
                </div>
            </section>
        </div>
    );
};

const FeaturePanel = ({eyebrow, title, text, icon: Icon, items}) => (
    <article className="rounded-2xl border border-slate-200 bg-[#f7f8f3] p-6">
        <span className="inline-flex h-11 w-11 items-center justify-center rounded-md bg-slate-950 text-[#d9ff72]">
            <Icon size={22} />
        </span>
        <p className="mt-5 text-xs font-bold uppercase tracking-wide text-emerald-700">{eyebrow}</p>
        <h3 className="mt-2 text-2xl font-bold">{title}</h3>
        <p className="mt-3 leading-7 text-slate-600">{text}</p>
        <div className="mt-5 grid gap-2 sm:grid-cols-2">
            {items.map((item) => (
                <div key={item} className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                    <ShieldCheck size={16} className="text-emerald-600" />
                    {item}
                </div>
            ))}
        </div>
    </article>
);

const SplitCard = ({name, amount, meta, status}) => (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
        <div className="flex items-start justify-between gap-3">
            <div>
                <p className="font-bold">{name}</p>
                <p className="mt-1 text-sm text-slate-500">{meta}</p>
            </div>
            <HandCoins size={20} className="text-emerald-700" />
        </div>
        <p className="mt-5 text-2xl font-bold">{amount}</p>
        <p className="mt-2 rounded-md bg-[#efffd0] px-3 py-2 text-sm font-semibold text-slate-800">{status}</p>
    </div>
);

const FamilyStat = ({label, value, text}) => (
    <div className="rounded-2xl border border-white/10 bg-white/[0.06] p-5">
        <UsersRound size={21} className="text-[#d9ff72]" />
        <p className="mt-4 text-sm text-white/55">{label}</p>
        <p className="text-2xl font-bold">{value}</p>
        <p className="text-sm text-white/55">{text}</p>
    </div>
);

const WorkflowCard = ({icon: Icon, title, text}) => (
    <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <Icon size={22} className="text-emerald-700" />
        <h3 className="mt-4 font-bold">{title}</h3>
        <p className="mt-2 text-sm leading-6 text-slate-600">{text}</p>
    </div>
);

const PlanCard = ({title, price, items, highlighted}) => (
    <div className={`rounded-2xl border p-6 ${highlighted ? "border-slate-950 bg-slate-950 text-white" : "border-slate-200 bg-white text-slate-950"}`}>
        <div className="flex items-center justify-between gap-3">
            <h3 className="text-2xl font-bold">{title}</h3>
            {highlighted && <BellRing size={21} className="text-[#d9ff72]" />}
        </div>
        <p className={`mt-2 text-lg font-semibold ${highlighted ? "text-[#d9ff72]" : "text-slate-600"}`}>{price}</p>
        <div className="mt-5 space-y-3">
            {items.map((item) => (
                <div key={item} className={`flex items-center gap-2 text-sm font-semibold ${highlighted ? "text-white/78" : "text-slate-700"}`}>
                    <CalendarClock size={16} className={highlighted ? "text-[#d9ff72]" : "text-emerald-700"} />
                    {item}
                </div>
            ))}
        </div>
    </div>
);

export default ProductShowcase;
