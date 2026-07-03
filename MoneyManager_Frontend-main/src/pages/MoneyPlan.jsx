import {useMemo, useState} from "react";
import toast from "react-hot-toast";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {Bar, BarChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis} from "recharts";
import {CalendarDays, CreditCard, IndianRupee, Landmark, Plus, Repeat2, Target, Trash2, WalletCards} from "lucide-react";
import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import {addThousandsSeparator} from "../util/util.js";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const paymentMethods = ["cash", "UPI", "card", "bank"];
const colors = ["#d9ff72", "#86efac", "#38bdf8", "#fbbf24", "#fb7185", "#a78bfa"];

const emptyBudget = {categoryId: "", amount: "", month: new Date().getMonth() + 1, year: new Date().getFullYear()};
const emptyGoal = {name: "", icon: "*", targetAmount: "", savedAmount: "", targetDate: ""};
const emptyRecurring = {name: "", icon: "*", type: "expense", frequency: "monthly", amount: "", categoryId: "", startDate: new Date().toISOString().slice(0, 10), nextRunDate: "", paymentMethod: "UPI", active: true};
const emptyReminder = {name: "", type: "bill", amount: "", dueDate: "", frequency: "monthly", paymentMethod: "UPI", categoryId: "", paid: false};
const emptyContribution = {goalId: "", amount: "", contributionDate: new Date().toISOString().slice(0, 10), note: ""};

const MoneyPlan = () => {
    useUser();
    const queryClient = useQueryClient();
    const [contributions, setContributions] = useState([]);
    const [budgetForm, setBudgetForm] = useState(emptyBudget);
    const [goalForm, setGoalForm] = useState(emptyGoal);
    const [recurringForm, setRecurringForm] = useState(emptyRecurring);
    const [reminderForm, setReminderForm] = useState(emptyReminder);
    const [contributionForm, setContributionForm] = useState(emptyContribution);
    const [activeView, setActiveView] = useState("overview");
    const selectedMonth = Number(budgetForm.month);
    const selectedYear = Number(budgetForm.year);

    const {data: planData = {}, isLoading: loading, refetch: loadData} = useQuery({
        queryKey: queryKeys.moneyPlan(selectedMonth, selectedYear),
        queryFn: async () => {
            const [categoryRes, budgetRes, goalRes, recurringRes, analyticsRes, summaryRes, reminderRes] = await Promise.all([
                axiosConfig.get(API_ENDPOINTS.GET_ALL_CATEGORIES),
                axiosConfig.get(API_ENDPOINTS.BUDGETS, {params: {month: selectedMonth, year: selectedYear}}),
                axiosConfig.get(API_ENDPOINTS.SAVINGS_GOALS),
                axiosConfig.get(API_ENDPOINTS.RECURRING_TRANSACTIONS),
                axiosConfig.get(API_ENDPOINTS.MONTHLY_ANALYTICS, {params: {month: selectedMonth, year: selectedYear}}),
                axiosConfig.get(API_ENDPOINTS.MONEY_PLAN_SUMMARY, {params: {month: selectedMonth, year: selectedYear, forecastDays: 30}}),
                axiosConfig.get(API_ENDPOINTS.BILL_REMINDERS),
            ]);
            return {
                categories: categoryRes.data || [],
                budgets: budgetRes.data || [],
                goals: goalRes.data || [],
                recurring: recurringRes.data || [],
                analytics: analyticsRes.data || null,
                summary: summaryRes.data || null,
                reminders: reminderRes.data || [],
            };
        },
        staleTime: cacheTimes.moneyPlan,
        onError: (error) => {
            console.error("Failed to load money plan:", error);
            toast.error("Failed to load money plan");
        },
    });

    const categories = planData.categories || [];
    const budgets = planData.budgets || [];
    const goals = planData.goals || [];
    const recurring = planData.recurring || [];
    const analytics = planData.analytics || null;
    const summary = planData.summary || null;
    const reminders = planData.reminders || [];
    const expenseCategories = useMemo(() => categories.filter((category) => category.type === "expense"), [categories]);

    const invalidateMoneyPlanCaches = () => {
        queryClient.invalidateQueries({queryKey: queryKeys.moneyPlan(selectedMonth, selectedYear)});
        queryClient.invalidateQueries({queryKey: queryKeys.dashboard});
        queryClient.invalidateQueries({queryKey: queryKeys.aiInsights});
    };

    const saveBudget = async (event) => {
        event.preventDefault();
        if (!budgetForm.categoryId || !budgetForm.amount) return toast.error("Budget category and amount are required");
        await axiosConfig.post(API_ENDPOINTS.BUDGETS, {...budgetForm, amount: Number(budgetForm.amount)});
        setBudgetForm(emptyBudget);
        toast.success("Budget saved");
        invalidateMoneyPlanCaches();
    };

    const saveGoal = async (event) => {
        event.preventDefault();
        if (!goalForm.name || !goalForm.targetAmount) return toast.error("Goal name and target are required");
        await axiosConfig.post(API_ENDPOINTS.SAVINGS_GOALS, {
            ...goalForm,
            targetAmount: Number(goalForm.targetAmount),
            savedAmount: Number(goalForm.savedAmount || 0),
        });
        setGoalForm(emptyGoal);
        toast.success("Goal saved");
        invalidateMoneyPlanCaches();
    };

    const saveRecurring = async (event) => {
        event.preventDefault();
        if (!recurringForm.name || !recurringForm.amount || !recurringForm.categoryId) return toast.error("Recurring name, category and amount are required");
        await axiosConfig.post(API_ENDPOINTS.RECURRING_TRANSACTIONS, {
            ...recurringForm,
            amount: Number(recurringForm.amount),
            nextRunDate: recurringForm.nextRunDate || recurringForm.startDate,
        });
        setRecurringForm(emptyRecurring);
        toast.success("Recurring item saved");
        invalidateMoneyPlanCaches();
    };

    const saveReminder = async (event) => {
        event.preventDefault();
        if (!reminderForm.name || !reminderForm.amount || !reminderForm.dueDate) return toast.error("Reminder name, amount and due date are required");
        await axiosConfig.post(API_ENDPOINTS.BILL_REMINDERS, {...reminderForm, amount: Number(reminderForm.amount), categoryId: reminderForm.categoryId || null});
        setReminderForm(emptyReminder);
        toast.success("Reminder saved");
        invalidateMoneyPlanCaches();
    };

    const saveContribution = async (event) => {
        event.preventDefault();
        if (!contributionForm.goalId || !contributionForm.amount) return toast.error("Choose a goal and amount");
        await axiosConfig.post(API_ENDPOINTS.GOAL_CONTRIBUTIONS(contributionForm.goalId), {...contributionForm, amount: Number(contributionForm.amount)});
        setContributionForm(emptyContribution);
        toast.success("Contribution added");
        invalidateMoneyPlanCaches();
        loadContributions(contributionForm.goalId);
    };

    const loadContributions = async (goalId) => {
        if (!goalId) {
            setContributions([]);
            return;
        }
        const response = await axiosConfig.get(API_ENDPOINTS.GOAL_CONTRIBUTIONS(goalId));
        setContributions(response.data || []);
    };

    const processRecurringDue = async () => {
        const response = await axiosConfig.post(API_ENDPOINTS.PROCESS_RECURRING_DUE);
        toast.success(`${response.data?.length || 0} due recurring item(s) created`);
        invalidateMoneyPlanCaches();
    };

    const remove = async (endpoint, id, message) => {
        await axiosConfig.delete(`${endpoint}/${id}`);
        toast.success(message);
        invalidateMoneyPlanCaches();
    };

    const calendarMap = new Map((analytics?.calendarSpend || []).map((item) => [item.date, Number(item.amount)]));
    const daysInMonth = new Date(selectedYear, selectedMonth, 0).getDate();
    const calendarDays = Array.from({length: daysInMonth}, (_, index) => {
        const day = index + 1;
        const date = `${selectedYear}-${String(selectedMonth).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
        return {day, date, amount: calendarMap.get(date) || 0};
    });

    return (
        <Dashboard activeMenu="Money Plan">
            <div className="mx-auto max-w-7xl">
                <div className="rounded-xl border border-white/14 bg-[#111713] p-4 text-white shadow-2xl shadow-black/20 md:p-6">
                    <section className="grid gap-4 xl:grid-cols-[1.1fr_0.9fr]">
                        <div className="rounded-xl border border-white/14 bg-[#18231d] p-5">
                            <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                                <div>
                                    <p className="text-xs font-semibold uppercase tracking-wide text-[#d9ff72]">Money Plan</p>
                                    <h1 className="mt-2 max-w-3xl text-3xl font-semibold leading-tight md:text-4xl">Plan the month from one command surface.</h1>
                                </div>
                                <div className="grid min-w-[260px] grid-cols-2 gap-2 rounded-lg border border-white/14 bg-black/15 p-2">
                                    <input className="dark-field" type="number" min="1" max="12" value={budgetForm.month} onChange={(e) => setBudgetForm({...budgetForm, month: e.target.value})} />
                                    <input className="dark-field" type="number" value={budgetForm.year} onChange={(e) => setBudgetForm({...budgetForm, year: e.target.value})} />
                                </div>
                            </div>
                            <div className="mt-6 grid gap-3 md:grid-cols-3">
                                <Metric icon={IndianRupee} label="This month" value={`Rs ${addThousandsSeparator(analytics?.currentMonthExpense || 0)}`} />
                                <Metric icon={CalendarDays} label="Last month" value={`Rs ${addThousandsSeparator(analytics?.previousMonthExpense || 0)}`} />
                                <Metric icon={WalletCards} label="Change" value={`${analytics?.monthChangePercent || 0}%`} />
                            </div>
                        </div>

                        <div className="rounded-xl border border-white/14 bg-[#18231d] p-5">
                            <p className="text-xs font-semibold uppercase tracking-wide text-white/65">Quick read</p>
                            <div className="mt-5 grid gap-3">
                                <InsightLine label="Budgets active" value={budgets.length} />
                                <InsightLine label="Savings goals" value={goals.length} />
                                <InsightLine label="Recurring rules" value={recurring.length} />
                                <InsightLine label="Budget alerts" value={summary?.budgetAlerts?.length || 0} />
                            </div>
                        </div>
                    </section>

                    <section className="mt-4 grid gap-4 xl:grid-cols-[0.95fr_1.05fr]">
                        <Panel title="Budget Alerts" action={`${summary?.budgetAlerts?.length || 0} alerts`}>
                            <div className="grid gap-2">
                                {(summary?.budgetAlerts || []).map((alert) => (
                                    <div key={alert.budgetId} className="rounded-lg border border-white/14 bg-white/[0.03] p-3">
                                        <p className="font-semibold text-white">{alert.message}</p>
                                        <p className="mt-1 text-xs text-white/65">Spent Rs {addThousandsSeparator(alert.spentAmount || 0)} of Rs {addThousandsSeparator(alert.budgetAmount || 0)}</p>
                                    </div>
                                ))}
                                {(summary?.budgetAlerts || []).length === 0 && <EmptyState text="No budget alerts. Everything is under control." />}
                            </div>
                        </Panel>

                        <Panel title="Cashflow Forecast" action="Next 30 days">
                            <div className="grid gap-3 md:grid-cols-4">
                                <ForecastTile label="Current" value={summary?.cashflowForecast?.currentBalance} />
                                <ForecastTile label="Incoming" value={summary?.cashflowForecast?.projectedIncome} />
                                <ForecastTile label="Outgoing" value={summary?.cashflowForecast?.projectedExpense} />
                                <ForecastTile label="Projected" value={summary?.cashflowForecast?.projectedBalance} highlight />
                            </div>
                        </Panel>
                    </section>

                    <ViewTabs
                        tabs={[
                            {id: "overview", label: "Overview"},
                            {id: "plan", label: "Plan items"},
                            {id: "calendar", label: "Calendar"},
                        ]}
                        active={activeView}
                        onChange={setActiveView}
                    />

                    {activeView === "plan" && <section className="mt-4 grid gap-4 xl:grid-cols-[0.85fr_1.15fr]">
                        <div className="rounded-xl border border-white/14 bg-[#18231d] p-4">
                            <div className="mb-4 flex items-center justify-between">
                                <div>
                                    <p className="text-xs font-semibold uppercase tracking-wide text-[#d9ff72]">Create</p>
                                    <h2 className="text-xl font-semibold">Planning actions</h2>
                                </div>
                                <span className="rounded-md border border-white/14 bg-white/5 px-2.5 py-1 text-xs text-white/78">Fast entry</span>
                            </div>

                            <div className="space-y-3">
                                <FeatureForm title="Monthly Budget" icon={CreditCard} onSubmit={saveBudget}>
                                    <select className="dark-field" value={budgetForm.categoryId} onChange={(e) => setBudgetForm({...budgetForm, categoryId: e.target.value})}>
                                        <option value="">Expense category</option>
                                        {expenseCategories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
                                    </select>
                                    <input className="dark-field" type="number" placeholder="Budget amount" value={budgetForm.amount} onChange={(e) => setBudgetForm({...budgetForm, amount: e.target.value})} />
                                </FeatureForm>

                                <FeatureForm title="Savings Goal" icon={Target} onSubmit={saveGoal}>
                                    <input className="dark-field" placeholder="Goal name" value={goalForm.name} onChange={(e) => setGoalForm({...goalForm, name: e.target.value})} />
                                    <div className="grid grid-cols-[68px_1fr] gap-2">
                                        <input className="dark-field" placeholder="Icon" value={goalForm.icon} onChange={(e) => setGoalForm({...goalForm, icon: e.target.value})} />
                                        <input className="dark-field" type="date" value={goalForm.targetDate} onChange={(e) => setGoalForm({...goalForm, targetDate: e.target.value})} />
                                    </div>
                                    <div className="grid grid-cols-2 gap-2">
                                        <input className="dark-field" type="number" placeholder="Target" value={goalForm.targetAmount} onChange={(e) => setGoalForm({...goalForm, targetAmount: e.target.value})} />
                                        <input className="dark-field" type="number" placeholder="Saved" value={goalForm.savedAmount} onChange={(e) => setGoalForm({...goalForm, savedAmount: e.target.value})} />
                                    </div>
                                </FeatureForm>

                                <FeatureForm title="Recurring Item" icon={Repeat2} onSubmit={saveRecurring}>
                                    <input className="dark-field" placeholder="Name" value={recurringForm.name} onChange={(e) => setRecurringForm({...recurringForm, name: e.target.value})} />
                                    <div className="grid grid-cols-2 gap-2">
                                        <select className="dark-field" value={recurringForm.type} onChange={(e) => setRecurringForm({...recurringForm, type: e.target.value})}>
                                            <option value="expense">Expense</option>
                                            <option value="income">Income</option>
                                        </select>
                                        <select className="dark-field" value={recurringForm.frequency} onChange={(e) => setRecurringForm({...recurringForm, frequency: e.target.value})}>
                                            <option value="weekly">Weekly</option>
                                            <option value="monthly">Monthly</option>
                                            <option value="yearly">Yearly</option>
                                        </select>
                                    </div>
                                    <select className="dark-field" value={recurringForm.categoryId} onChange={(e) => setRecurringForm({...recurringForm, categoryId: e.target.value})}>
                                        <option value="">Category</option>
                                        {categories.filter((category) => category.type === recurringForm.type).map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
                                    </select>
                                    <div className="grid grid-cols-2 gap-2">
                                        <input className="dark-field" type="number" placeholder="Amount" value={recurringForm.amount} onChange={(e) => setRecurringForm({...recurringForm, amount: e.target.value})} />
                                        <select className="dark-field" value={recurringForm.paymentMethod} onChange={(e) => setRecurringForm({...recurringForm, paymentMethod: e.target.value})}>
                                            {paymentMethods.map((method) => <option key={method}>{method}</option>)}
                                        </select>
                                    </div>
                                </FeatureForm>

                                <FeatureForm title="Bill / Subscription" icon={CalendarDays} onSubmit={saveReminder}>
                                    <input className="dark-field" placeholder="Name" value={reminderForm.name} onChange={(e) => setReminderForm({...reminderForm, name: e.target.value})} />
                                    <div className="grid grid-cols-2 gap-2">
                                        <select className="dark-field" value={reminderForm.type} onChange={(e) => setReminderForm({...reminderForm, type: e.target.value})}>
                                            <option value="bill">Bill</option>
                                            <option value="subscription">Subscription</option>
                                        </select>
                                        <input className="dark-field" type="date" value={reminderForm.dueDate} onChange={(e) => setReminderForm({...reminderForm, dueDate: e.target.value})} />
                                    </div>
                                    <div className="grid grid-cols-2 gap-2">
                                        <input className="dark-field" type="number" placeholder="Amount" value={reminderForm.amount} onChange={(e) => setReminderForm({...reminderForm, amount: e.target.value})} />
                                        <select className="dark-field" value={reminderForm.paymentMethod} onChange={(e) => setReminderForm({...reminderForm, paymentMethod: e.target.value})}>
                                            {paymentMethods.map((method) => <option key={method}>{method}</option>)}
                                        </select>
                                    </div>
                                </FeatureForm>

                                <FeatureForm title="Goal Contribution" icon={Target} onSubmit={saveContribution}>
                                    <select className="dark-field" value={contributionForm.goalId} onChange={(e) => {
                                        setContributionForm({...contributionForm, goalId: e.target.value});
                                        loadContributions(e.target.value);
                                    }}>
                                        <option value="">Savings goal</option>
                                        {goals.map((goal) => <option key={goal.id} value={goal.id}>{goal.name}</option>)}
                                    </select>
                                    <div className="grid grid-cols-2 gap-2">
                                        <input className="dark-field" type="number" placeholder="Amount" value={contributionForm.amount} onChange={(e) => setContributionForm({...contributionForm, amount: e.target.value})} />
                                        <input className="dark-field" type="date" value={contributionForm.contributionDate} onChange={(e) => setContributionForm({...contributionForm, contributionDate: e.target.value})} />
                                    </div>
                                    <input className="dark-field" placeholder="Note" value={contributionForm.note} onChange={(e) => setContributionForm({...contributionForm, note: e.target.value})} />
                                </FeatureForm>
                            </div>
                        </div>

                        <div className="grid gap-4">
                            <Panel title="Where did my money go?" action="Category breakdown">
                                <div className="grid gap-4 md:grid-cols-[220px_1fr]">
                                    <div className="h-56">
                                        <ResponsiveContainer width="100%" height="100%">
                                            <PieChart>
                                                <Pie data={analytics?.categoryBreakdown || []} dataKey="amount" nameKey="categoryName" innerRadius={58} outerRadius={92}>
                                                    {(analytics?.categoryBreakdown || []).map((entry, index) => <Cell key={entry.categoryId} fill={colors[index % colors.length]} />)}
                                                </Pie>
                                                <Tooltip contentStyle={{background: "#18231d", border: "1px solid rgba(255,255,255,.12)", color: "#fff"}} />
                                            </PieChart>
                                        </ResponsiveContainer>
                                    </div>
                                    <div className="space-y-2">
                                        {(analytics?.categoryBreakdown || []).map((item) => (
                                            <div key={item.categoryId} className="flex items-center justify-between rounded-lg border border-white/14 bg-white/[0.03] px-3 py-2.5">
                                                <span className="flex items-center gap-2 text-sm text-white/80"><DisplayIcon icon={item.icon} label={item.categoryName} />{item.categoryName}</span>
                                                <span className="text-sm font-semibold">Rs {addThousandsSeparator(item.amount)} | {item.percent}%</span>
                                            </div>
                                        ))}
                                        {(analytics?.categoryBreakdown || []).length === 0 && <EmptyState text="Add expenses to see category insights." />}
                                    </div>
                                </div>
                            </Panel>

                            <Panel title="Monthly Comparison" action="Expense trend">
                                <div className="h-56">
                                    <ResponsiveContainer width="100%" height="100%">
                                        <BarChart data={[
                                            {name: "Last month", amount: Number(analytics?.previousMonthExpense || 0)},
                                            {name: "This month", amount: Number(analytics?.currentMonthExpense || 0)},
                                        ]}>
                                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="rgba(255,255,255,.08)" />
                                            <XAxis dataKey="name" stroke="rgba(255,255,255,.45)" />
                                            <YAxis stroke="rgba(255,255,255,.45)" />
                                            <Tooltip contentStyle={{background: "#18231d", border: "1px solid rgba(255,255,255,.12)", color: "#fff"}} />
                                            <Bar dataKey="amount" fill="#d9ff72" radius={[8, 8, 0, 0]} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            </Panel>
                        </div>
                    </section>}

                    {activeView === "overview" && <section className="mt-4 grid gap-4 xl:grid-cols-[1fr_1fr_0.9fr]">
                        <Panel title="Budget Progress" action={loading ? "Loading" : `${budgets.length} active`}>
                            <div className="space-y-4">
                                {budgets.map((budget) => <ProgressRow key={budget.id} icon={budget.categoryIcon} title={budget.categoryName} subtitle={`Spent Rs ${addThousandsSeparator(budget.spent || 0)} of Rs ${addThousandsSeparator(budget.amount || 0)}`} percent={budget.usagePercent || 0} onDelete={() => remove(API_ENDPOINTS.BUDGETS, budget.id, "Budget deleted")} />)}
                                {budgets.length === 0 && <EmptyState text="No budgets for this month yet." />}
                            </div>
                        </Panel>

                        <Panel title="Savings Goals" action={`${goals.length} goals`}>
                            <div className="space-y-4">
                                {goals.map((goal) => <ProgressRow key={goal.id} icon={goal.icon} title={goal.name} subtitle={`Rs ${addThousandsSeparator(goal.savedAmount || 0)} saved of Rs ${addThousandsSeparator(goal.targetAmount || 0)}`} percent={goal.progressPercent || 0} onDelete={() => remove(API_ENDPOINTS.SAVINGS_GOALS, goal.id, "Goal deleted")} />)}
                                {goals.length === 0 && <EmptyState text="No savings goals yet." />}
                            </div>
                        </Panel>

                        <Panel title="Recurring Rules" action={`${recurring.length} rules`}>
                            <button className="mb-3 inline-flex rounded-md bg-[#d9ff72] px-3 py-2 text-xs font-semibold text-[#1f2a24]" onClick={processRecurringDue}>Create due transactions</button>
                            <div className="space-y-2">
                                {recurring.map((item) => (
                                    <div key={item.id} className="flex items-center justify-between rounded-lg border border-white/14 bg-white/[0.03] p-3">
                                        <div>
                                            <p className="flex items-center gap-2 font-medium text-white"><DisplayIcon icon={item.icon} label={item.name} /> {item.name}</p>
                                            <p className="text-xs text-white/65">{item.frequency} | {item.paymentMethod} | {item.categoryName}</p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <p className={item.type === "income" ? "font-semibold text-emerald-300" : "font-semibold text-rose-300"}>Rs {addThousandsSeparator(item.amount)}</p>
                                            <button className="dark-icon-btn" onClick={() => remove(API_ENDPOINTS.RECURRING_TRANSACTIONS, item.id, "Recurring item deleted")}><Trash2 size={16} /></button>
                                        </div>
                                    </div>
                                ))}
                                {recurring.length === 0 && <EmptyState text="No recurring rules yet." />}
                            </div>
                        </Panel>
                    </section>}

                    {activeView === "plan" && <section className="mt-4 grid gap-4 xl:grid-cols-2">
                        <Panel title="Bills & Subscriptions" action={`${reminders.length} reminders`}>
                            <div className="space-y-2">
                                {reminders.map((item) => (
                                    <div key={item.id} className="flex items-center justify-between rounded-lg border border-white/14 bg-white/[0.03] p-3">
                                        <div>
                                            <p className="font-medium text-white">{item.name}</p>
                                            <p className="text-xs text-white/65">{item.type} | due {item.dueDate} | {item.daysUntilDue} days</p>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <p className="font-semibold text-[#d9ff72]">Rs {addThousandsSeparator(item.amount || 0)}</p>
                                            <button className="dark-icon-btn" onClick={() => remove(API_ENDPOINTS.BILL_REMINDERS, item.id, "Reminder deleted")}><Trash2 size={16} /></button>
                                        </div>
                                    </div>
                                ))}
                                {reminders.length === 0 && <EmptyState text="No bills or subscriptions saved yet." />}
                            </div>
                        </Panel>

                        <Panel title="Contribution History" action={`${contributions.length} entries`}>
                            <div className="space-y-2">
                                {contributions.map((item) => (
                                    <div key={item.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-3">
                                        <p className="font-semibold text-white">Rs {addThousandsSeparator(item.amount || 0)}</p>
                                        <p className="text-xs text-white/65">{item.contributionDate} {item.note ? `| ${item.note}` : ""}</p>
                                    </div>
                                ))}
                                {contributions.length === 0 && <EmptyState text="Select a goal to view contribution history." />}
                            </div>
                        </Panel>
                    </section>}

                    {activeView === "calendar" && <section className="mt-4">
                        <Panel title="Spending Calendar" action={`${selectedMonth}/${selectedYear}`}>
                            <div className="grid grid-cols-7 gap-1.5 md:gap-2">
                                {calendarDays.map((day) => (
                                    <div key={day.date} className={`aspect-square rounded-lg border p-1.5 text-xs ${day.amount > 0 ? "border-[#d9ff72]/40 bg-[#d9ff72]/15 text-[#d9ff72]" : "border-white/14 bg-white/[0.03] text-white/78"}`}>
                                        <p>{day.day}</p>
                                        {day.amount > 0 && <p className="mt-1 truncate font-semibold">Rs {day.amount}</p>}
                                    </div>
                                ))}
                            </div>
                        </Panel>
                    </section>}
                </div>
            </div>
        </Dashboard>
    );
};

const Metric = ({icon: Icon, label, value}) => (
    <div className="rounded-xl border border-white/14 bg-white/[0.04] p-4">
        <Icon className="mb-3 text-[#d9ff72]" size={22} />
        <p className="text-xs uppercase text-white/65">{label}</p>
        <p className="mt-1 text-xl font-semibold text-white">{value}</p>
    </div>
);

const InsightLine = ({label, value}) => (
    <div className="flex items-center justify-between rounded-lg border border-white/14 bg-white/[0.03] px-4 py-3">
        <span className="text-sm text-white/78">{label}</span>
        <span className="text-lg font-semibold text-white">{value}</span>
    </div>
);

const ForecastTile = ({label, value, highlight}) => (
    <div className={`rounded-lg border p-4 ${highlight ? "border-[#d9ff72]/40 bg-[#d9ff72]/10" : "border-white/14 bg-white/[0.03]"}`}>
        <p className="text-xs font-semibold uppercase text-white/65">{label}</p>
        <p className={`mt-2 text-xl font-semibold ${highlight ? "text-[#d9ff72]" : "text-white"}`}>Rs {addThousandsSeparator(value || 0)}</p>
    </div>
);

const FeatureForm = ({title, icon: Icon, onSubmit, children}) => (
    <form onSubmit={onSubmit} className="rounded-xl border border-white/14 bg-black/15 p-4">
        <div className="mb-4 flex items-center justify-between">
            <h2 className="flex items-center gap-2 text-base font-semibold text-white"><Icon size={18} /> {title}</h2>
            <button className="inline-flex h-9 w-9 items-center justify-center rounded-md bg-[#d9ff72] text-[#1f2a24] hover:bg-[#c9f35b]" type="submit"><Plus size={18} /></button>
        </div>
        <div className="space-y-2.5">{children}</div>
    </form>
);

const Panel = ({title, action, children}) => (
    <div className="rounded-xl border border-white/14 bg-[#18231d] p-4 text-white">
        <div className="mb-4 flex items-center justify-between gap-3">
            <h2 className="text-lg font-semibold">{title}</h2>
            <span className="rounded-md border border-white/14 bg-white/5 px-2.5 py-1 text-xs font-medium text-white/68">{action}</span>
        </div>
        {children}
    </div>
);

const ViewTabs = ({tabs, active, onChange}) => (
    <div className="sticky top-[76px] z-10 mt-4 rounded-lg border border-white/14 bg-[#172119]/95 p-1.5 shadow-xl shadow-black/20 backdrop-blur">
        <div className="grid gap-1 sm:grid-cols-3">
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    type="button"
                    onClick={() => onChange(tab.id)}
                    className={`rounded-md px-3 py-2.5 text-sm font-semibold transition ${active === tab.id ? "bg-[#d9ff72] text-[#1f2a24]" : "text-white/70 hover:bg-white/10 hover:text-white"}`}
                >
                    {tab.label}
                </button>
            ))}
        </div>
    </div>
);

const ProgressRow = ({icon, title, subtitle, percent, onDelete}) => (
    <div className="rounded-lg border border-white/14 bg-white/[0.03] p-3">
        <div className="mb-3 flex items-center justify-between gap-3">
            <div className="min-w-0">
                <p className="flex items-center gap-2 font-medium text-white">
                    <DisplayIcon icon={icon} label={title} />
                    <span className="min-w-0 truncate">{title}</span>
                </p>
                <p className="text-xs text-white/65">{subtitle}</p>
            </div>
            <button className="dark-icon-btn" onClick={onDelete}><Trash2 size={16} /></button>
        </div>
        <div className="h-2 overflow-hidden rounded-full bg-white/10">
            <div className="h-full rounded-full bg-[#d9ff72]" style={{width: `${Math.min(Number(percent), 100)}%`}} />
        </div>
        <p className="mt-1 text-right text-xs font-medium text-white/65">{Number(percent).toFixed(1)}%</p>
    </div>
);

const DisplayIcon = ({icon, label}) => {
    if (!icon || icon === "*") {
        return <span className="grid h-7 w-7 shrink-0 place-items-center rounded-md bg-white/10 text-xs font-semibold text-[#d9ff72]">{label?.slice(0, 2)?.toUpperCase() || "₹"}</span>;
    }

    if (typeof icon === "string" && /^https?:\/\//i.test(icon)) {
        return <img src={icon} alt={label || "icon"} className="h-6 w-6 shrink-0 rounded-sm object-contain" />;
    }

    return <span className="shrink-0 text-lg leading-none">{icon}</span>;
};

const EmptyState = ({text}) => (
    <div className="rounded-lg border border-dashed border-white/14 bg-white/[0.03] px-4 py-6 text-center text-sm text-white/65">
        <Landmark className="mx-auto mb-2 text-white/30" size={22} />
        {text}
    </div>
);

export default MoneyPlan;

