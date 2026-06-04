import {useEffect, useMemo, useState} from "react";
import toast from "react-hot-toast";
import {Bell, Check, HandCoins, MessageSquare, Plus, ReceiptText, RefreshCcw, UsersRound, WalletCards} from "lucide-react";
import Dashboard from "../components/Dashboard.jsx";
import PageHeader from "../components/PageHeader.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import {useUser} from "../hooks/useUser.jsx";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const today = new Date().toISOString().slice(0, 10);
const money = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;

const Friends = () => {
    useUser();
    const queryClient = useQueryClient();
    const [friendForm, setFriendForm] = useState({name: "", email: "", phone: "", upiId: "", status: "active"});
    const [groupForm, setGroupForm] = useState({name: "", type: "Trip", icon: "TR", friendIds: []});
    const [expenseForm, setExpenseForm] = useState({title: "", amount: "", category: "Food", expenseDate: today, splitType: "equal", paidByFriendId: "", groupId: "", note: ""});
    const [splitDraft, setSplitDraft] = useState({});
    const [settlementForm, setSettlementForm] = useState({friendId: "", amount: "", settlementDate: today, method: "UPI", direction: "friend_paid_you", note: ""});
    const [reminderForm, setReminderForm] = useState({friendId: "", message: "", amount: "", dueDate: today});
    const [commentForm, setCommentForm] = useState({expenseId: "", comment: ""});
    const {data = {friends: [], groups: [], expenses: [], settlements: [], reminders: [], activities: []}, isLoading: loading, refetch: loadData} = useQuery({
        queryKey: queryKeys.friendsDashboard,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.FRIENDS_DASHBOARD);
            return response.data || {};
        },
        staleTime: cacheTimes.friends,
        onError: (error) => toast.error(error.response?.data?.message || "Failed to load friends"),
    });

    const friends = data.friends || [];
    const groups = data.groups || [];
    const expenses = data.expenses || [];
    const activities = data.activities || [];

    const selectedFriendIds = useMemo(() => {
        if (expenseForm.groupId) {
            return groups.find((group) => String(group.id) === String(expenseForm.groupId))?.friendIds || [];
        }
        return friends.map((friend) => friend.id);
    }, [expenseForm.groupId, groups, friends]);

    const invalidateFriendsCaches = () => {
        queryClient.invalidateQueries({queryKey: queryKeys.friendsDashboard});
        queryClient.invalidateQueries({queryKey: queryKeys.dashboard});
        queryClient.invalidateQueries({queryKey: queryKeys.aiInsights});
    };

    useEffect(() => {
        const firstFriend = friends[0]?.id || "";
        if (!settlementForm.friendId && firstFriend) setSettlementForm((prev) => ({...prev, friendId: firstFriend}));
        if (!reminderForm.friendId && firstFriend) setReminderForm((prev) => ({...prev, friendId: firstFriend}));
    }, [friends, settlementForm.friendId, reminderForm.friendId]);

    const addFriend = async (event) => {
        event.preventDefault();
        if (!friendForm.name.trim()) return toast.error("Friend name is required");
        await axiosConfig.post(API_ENDPOINTS.FRIENDS, friendForm);
        setFriendForm({name: "", email: "", phone: "", upiId: "", status: "active"});
        toast.success("Friend added");
        invalidateFriendsCaches();
    };

    const updateStatus = async (friendId, status) => {
        await axiosConfig.patch(API_ENDPOINTS.FRIEND_STATUS(friendId), {status});
        toast.success("Friend status updated");
        invalidateFriendsCaches();
    };

    const addGroup = async (event) => {
        event.preventDefault();
        if (!groupForm.name.trim()) return toast.error("Group name is required");
        await axiosConfig.post(API_ENDPOINTS.FRIEND_GROUPS, groupForm);
        setGroupForm({name: "", type: "Trip", icon: "TR", friendIds: []});
        toast.success("Group created");
        invalidateFriendsCaches();
    };

    const addSharedExpense = async (event) => {
        event.preventDefault();
        if (!expenseForm.title || !expenseForm.amount) return toast.error("Title and amount are required");
        const splits = selectedFriendIds.map((friendId) => ({
            friendId,
            amount: Number(splitDraft[friendId]?.amount || 0),
            percentValue: Number(splitDraft[friendId]?.percentValue || 0),
            shares: Number(splitDraft[friendId]?.shares || 1),
        }));
        await axiosConfig.post(API_ENDPOINTS.FRIEND_EXPENSES, {
            ...expenseForm,
            amount: Number(expenseForm.amount),
            paidByFriendId: expenseForm.paidByFriendId || null,
            groupId: expenseForm.groupId || null,
            splits,
        });
        setExpenseForm({title: "", amount: "", category: "Food", expenseDate: today, splitType: "equal", paidByFriendId: "", groupId: "", note: ""});
        setSplitDraft({});
        toast.success("Shared expense added");
        invalidateFriendsCaches();
    };

    const settleUp = async (event) => {
        event.preventDefault();
        if (!settlementForm.friendId || !settlementForm.amount) return toast.error("Choose friend and amount");
        await axiosConfig.post(API_ENDPOINTS.FRIEND_SETTLEMENTS, {...settlementForm, amount: Number(settlementForm.amount)});
        setSettlementForm({...settlementForm, amount: "", note: ""});
        toast.success("Settlement recorded");
        invalidateFriendsCaches();
    };

    const addReminder = async (event) => {
        event.preventDefault();
        if (!reminderForm.friendId || !reminderForm.message) return toast.error("Choose friend and message");
        await axiosConfig.post(API_ENDPOINTS.FRIEND_REMINDERS, {...reminderForm, amount: Number(reminderForm.amount || 0)});
        setReminderForm({...reminderForm, message: "", amount: ""});
        toast.success("Reminder added");
        invalidateFriendsCaches();
    };

    const addComment = async (event) => {
        event.preventDefault();
        if (!commentForm.expenseId || !commentForm.comment) return toast.error("Choose expense and comment");
        await axiosConfig.post(API_ENDPOINTS.FRIEND_EXPENSE_COMMENTS(commentForm.expenseId), {comment: commentForm.comment});
        setCommentForm({expenseId: "", comment: ""});
        toast.success("Comment added");
        invalidateFriendsCaches();
    };

    const toggleGroupFriend = (friendId) => {
        const exists = groupForm.friendIds.includes(friendId);
        setGroupForm({...groupForm, friendIds: exists ? groupForm.friendIds.filter((id) => id !== friendId) : [...groupForm.friendIds, friendId]});
    };

    return (
        <Dashboard activeMenu="Friends">
            <div className="mx-auto max-w-7xl space-y-5">
                <PageHeader
                    eyebrow="Split with friends"
                    title="Friends"
                    description="Add friends, split shared expenses, track who owes whom, settle up, and keep reminders in one place."
                    action={<button onClick={loadData} className="add-btn"><RefreshCcw size={16} /> Refresh</button>}
                />

                <section className="grid gap-4 md:grid-cols-3">
                    <Summary icon={WalletCards} label="Owed to you" value={money(data.totalOwedToYou)} tone="text-[#d9ff72]" />
                    <Summary icon={HandCoins} label="You owe" value={money(data.totalYouOwe)} tone="text-rose-300" />
                    <Summary icon={ReceiptText} label="Net balance" value={money(data.netBalance)} tone="text-white" />
                </section>

                <section className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
                    <Panel title="Add friend">
                        <form onSubmit={addFriend} className="grid gap-3">
                            <input className="input-box" placeholder="Name" value={friendForm.name} onChange={(e) => setFriendForm({...friendForm, name: e.target.value})} />
                            <div className="grid gap-3 md:grid-cols-2">
                                <input className="input-box" placeholder="Email" value={friendForm.email} onChange={(e) => setFriendForm({...friendForm, email: e.target.value})} />
                                <input className="input-box" placeholder="Phone" value={friendForm.phone} onChange={(e) => setFriendForm({...friendForm, phone: e.target.value})} />
                            </div>
                            <div className="grid gap-3 md:grid-cols-2">
                                <input className="input-box" placeholder="UPI ID" value={friendForm.upiId} onChange={(e) => setFriendForm({...friendForm, upiId: e.target.value})} />
                                <select className="input-box" value={friendForm.status} onChange={(e) => setFriendForm({...friendForm, status: e.target.value})}>
                                    <option value="active">Active</option>
                                    <option value="invited">Invited</option>
                                    <option value="blocked">Blocked</option>
                                </select>
                            </div>
                            <button className="add-btn add-btn-fill"><Plus size={16} /> Add Friend</button>
                        </form>
                    </Panel>

                    <Panel title="Friend balances" action={`${friends.length} friends`}>
                        <div className="grid gap-3 md:grid-cols-2">
                            {friends.map((friend) => (
                                <div key={friend.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                    <div className="flex items-start justify-between gap-3">
                                        <div>
                                            <p className="font-semibold text-white">{friend.name}</p>
                                            <p className="text-xs text-white/65">{friend.email || friend.phone || friend.inviteCode}</p>
                                        </div>
                                        <span className={`rounded-md px-2 py-1 text-xs ${friend.status === "active" ? "bg-emerald-400/10 text-emerald-200" : "bg-white/10 text-white/78"}`}>{friend.status}</span>
                                    </div>
                                    <p className={`mt-3 text-xl font-black ${Number(friend.balance) >= 0 ? "text-[#d9ff72]" : "text-rose-300"}`}>
                                        {Number(friend.balance) >= 0 ? `${friend.name} owes you ` : `You owe ${friend.name} `}{money(Math.abs(Number(friend.balance || 0)))}
                                    </p>
                                    <div className="mt-3 flex gap-2">
                                        <button className="dark-icon-btn px-3 text-xs" onClick={() => updateStatus(friend.id, "active")}>Accept</button>
                                        <button className="dark-icon-btn px-3 text-xs" onClick={() => updateStatus(friend.id, "blocked")}>Block</button>
                                    </div>
                                </div>
                            ))}
                            {!loading && friends.length === 0 && <Empty text="Add friends to start splitting expenses." />}
                        </div>
                    </Panel>
                </section>

                <section className="grid gap-4 xl:grid-cols-[1fr_0.9fr]">
                    <Panel title="Add shared expense">
                        <form onSubmit={addSharedExpense} className="grid gap-3">
                            <div className="grid gap-3 md:grid-cols-2">
                                <input className="input-box" placeholder="Dinner, trip hotel, groceries..." value={expenseForm.title} onChange={(e) => setExpenseForm({...expenseForm, title: e.target.value})} />
                                <input className="input-box" type="number" placeholder="Amount" value={expenseForm.amount} onChange={(e) => setExpenseForm({...expenseForm, amount: e.target.value})} />
                            </div>
                            <div className="grid gap-3 md:grid-cols-4">
                                <input className="input-box" placeholder="Category" value={expenseForm.category} onChange={(e) => setExpenseForm({...expenseForm, category: e.target.value})} />
                                <input className="input-box" type="date" value={expenseForm.expenseDate} onChange={(e) => setExpenseForm({...expenseForm, expenseDate: e.target.value})} />
                                <select className="input-box" value={expenseForm.splitType} onChange={(e) => setExpenseForm({...expenseForm, splitType: e.target.value})}>
                                    <option value="equal">Equal</option>
                                    <option value="exact">Exact amount</option>
                                    <option value="percentage">Percentage</option>
                                    <option value="shares">Shares</option>
                                </select>
                                <select className="input-box" value={expenseForm.paidByFriendId} onChange={(e) => setExpenseForm({...expenseForm, paidByFriendId: e.target.value})}>
                                    <option value="">You paid</option>
                                    {friends.map((friend) => <option key={friend.id} value={friend.id}>{friend.name} paid</option>)}
                                </select>
                            </div>
                            <select className="input-box" value={expenseForm.groupId} onChange={(e) => setExpenseForm({...expenseForm, groupId: e.target.value})}>
                                <option value="">All friends / no group</option>
                                {groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}
                            </select>
                            {expenseForm.splitType !== "equal" && (
                                <div className="grid gap-2 md:grid-cols-2">
                                    {friends.filter((friend) => selectedFriendIds.includes(friend.id)).map((friend) => (
                                        <input
                                            key={friend.id}
                                            className="input-box"
                                            type="number"
                                            placeholder={`${friend.name} ${expenseForm.splitType}`}
                                            value={splitDraft[friend.id]?.[expenseForm.splitType === "percentage" ? "percentValue" : expenseForm.splitType === "shares" ? "shares" : "amount"] || ""}
                                            onChange={(e) => setSplitDraft({...splitDraft, [friend.id]: {...splitDraft[friend.id], [expenseForm.splitType === "percentage" ? "percentValue" : expenseForm.splitType === "shares" ? "shares" : "amount"]: e.target.value}})}
                                        />
                                    ))}
                                </div>
                            )}
                            <input className="input-box" placeholder="Note" value={expenseForm.note} onChange={(e) => setExpenseForm({...expenseForm, note: e.target.value})} />
                            <button className="add-btn add-btn-fill"><ReceiptText size={16} /> Split Expense</button>
                        </form>
                    </Panel>

                    <Panel title="Create group">
                        <form onSubmit={addGroup} className="grid gap-3">
                            <div className="grid grid-cols-[80px_1fr] gap-3">
                                <input className="input-box" placeholder="Icon" value={groupForm.icon} onChange={(e) => setGroupForm({...groupForm, icon: e.target.value})} />
                                <input className="input-box" placeholder="Group name" value={groupForm.name} onChange={(e) => setGroupForm({...groupForm, name: e.target.value})} />
                            </div>
                            <select className="input-box" value={groupForm.type} onChange={(e) => setGroupForm({...groupForm, type: e.target.value})}>
                                <option>Trip</option>
                                <option>Roommates</option>
                                <option>Family</option>
                                <option>Office Lunch</option>
                            </select>
                            <div className="flex flex-wrap gap-2">
                                {friends.map((friend) => (
                                    <button type="button" key={friend.id} onClick={() => toggleGroupFriend(friend.id)} className={`rounded-md border px-3 py-2 text-sm ${groupForm.friendIds.includes(friend.id) ? "border-[#d9ff72] bg-[#d9ff72] text-[#1f2a24]" : "border-white/14 bg-white/5 text-white/78"}`}>
                                        {friend.name}
                                    </button>
                                ))}
                            </div>
                            <button className="add-btn add-btn-fill"><UsersRound size={16} /> Create Group</button>
                        </form>
                    </Panel>
                </section>

                <section className="grid gap-4 xl:grid-cols-3">
                    <Panel title="Settle up">
                        <form onSubmit={settleUp} className="grid gap-3">
                            <FriendSelect friends={friends} value={settlementForm.friendId} onChange={(friendId) => setSettlementForm({...settlementForm, friendId})} />
                            <input className="input-box" type="number" placeholder="Amount" value={settlementForm.amount} onChange={(e) => setSettlementForm({...settlementForm, amount: e.target.value})} />
                            <select className="input-box" value={settlementForm.direction} onChange={(e) => setSettlementForm({...settlementForm, direction: e.target.value})}>
                                <option value="friend_paid_you">Friend paid you</option>
                                <option value="you_paid_friend">You paid friend</option>
                            </select>
                            <select className="input-box" value={settlementForm.method} onChange={(e) => setSettlementForm({...settlementForm, method: e.target.value})}>
                                <option>UPI</option><option>cash</option><option>bank</option><option>card</option>
                            </select>
                            <button className="add-btn add-btn-fill"><Check size={16} /> Mark Settled</button>
                        </form>
                    </Panel>

                    <Panel title="Reminder">
                        <form onSubmit={addReminder} className="grid gap-3">
                            <FriendSelect friends={friends} value={reminderForm.friendId} onChange={(friendId) => setReminderForm({...reminderForm, friendId})} />
                            <input className="input-box" placeholder="Rahul still owes ₹500" value={reminderForm.message} onChange={(e) => setReminderForm({...reminderForm, message: e.target.value})} />
                            <div className="grid grid-cols-2 gap-3">
                                <input className="input-box" type="number" placeholder="Amount" value={reminderForm.amount} onChange={(e) => setReminderForm({...reminderForm, amount: e.target.value})} />
                                <input className="input-box" type="date" value={reminderForm.dueDate} onChange={(e) => setReminderForm({...reminderForm, dueDate: e.target.value})} />
                            </div>
                            <button className="add-btn add-btn-fill"><Bell size={16} /> Add Reminder</button>
                        </form>
                    </Panel>

                    <Panel title="Comment">
                        <form onSubmit={addComment} className="grid gap-3">
                            <select className="input-box" value={commentForm.expenseId} onChange={(e) => setCommentForm({...commentForm, expenseId: e.target.value})}>
                                <option value="">Choose expense</option>
                                {expenses.map((expense) => <option key={expense.id} value={expense.id}>{expense.title}</option>)}
                            </select>
                            <input className="input-box" placeholder="Goa trip hotel advance..." value={commentForm.comment} onChange={(e) => setCommentForm({...commentForm, comment: e.target.value})} />
                            <button className="add-btn add-btn-fill"><MessageSquare size={16} /> Add Comment</button>
                        </form>
                    </Panel>
                </section>

                <section className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
                    <Panel title="Shared expenses" action={`${expenses.length} entries`}>
                        <div className="space-y-3">
                            {expenses.map((expense) => (
                                <div key={expense.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                    <div className="flex items-start justify-between gap-4">
                                        <div>
                                            <p className="font-semibold text-white">{expense.title}</p>
                                            <p className="text-xs text-white/65">{expense.expenseDate} | {expense.category} | {expense.paidByFriendName || "You"} paid | {expense.splitType}</p>
                                        </div>
                                        <p className="text-lg font-black text-[#d9ff72]">{money(expense.amount)}</p>
                                    </div>
                                    <div className="mt-3 flex flex-wrap gap-2">
                                        {(expense.splits || []).map((split) => <span key={split.friendId} className="rounded-md bg-black/25 px-2 py-1 text-xs text-white/72">{split.friendName}: {money(split.amount)}</span>)}
                                    </div>
                                    {(expense.comments || []).length > 0 && <p className="mt-3 text-xs text-white/65">{expense.comments[0].comment}</p>}
                                </div>
                            ))}
                            {expenses.length === 0 && <Empty text="No shared expenses yet." />}
                        </div>
                    </Panel>

                    <Panel title="Activity timeline">
                        <div className="space-y-3">
                            {activities.map((activity) => (
                                <div key={activity.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-3">
                                    <p className="text-sm font-semibold text-white">{activity.message}</p>
                                    <p className="mt-1 text-xs text-white/72">{activity.type}</p>
                                </div>
                            ))}
                            {activities.length === 0 && <Empty text="Activity will appear here." />}
                        </div>
                    </Panel>
                </section>
            </div>
        </Dashboard>
    );
};

const Summary = ({icon: Icon, label, value, tone}) => (
    <div className="rounded-lg border border-white/14 bg-[#1f2a24] p-5 text-white">
        <Icon className="text-[#d9ff72]" size={22} />
        <p className="mt-4 text-xs uppercase text-white/65">{label}</p>
        <p className={`mt-1 text-2xl font-black ${tone}`}>{value}</p>
    </div>
);

const Panel = ({title, action, children}) => (
    <div className="rounded-lg border border-white/14 bg-[#1f2a24] p-5 text-white">
        <div className="mb-4 flex items-center justify-between gap-3">
            <h2 className="text-lg font-bold">{title}</h2>
            {action && <span className="rounded-md border border-white/14 bg-white/5 px-2 py-1 text-xs text-white/65">{action}</span>}
        </div>
        {children}
    </div>
);

const FriendSelect = ({friends, value, onChange}) => (
    <select className="input-box" value={value} onChange={(e) => onChange(e.target.value)}>
        <option value="">Choose friend</option>
        {friends.map((friend) => <option key={friend.id} value={friend.id}>{friend.name}</option>)}
    </select>
);

const Empty = ({text}) => (
    <div className="rounded-lg border border-dashed border-white/14 bg-white/[0.03] p-5 text-center text-sm text-white/65">{text}</div>
);

export default Friends;

