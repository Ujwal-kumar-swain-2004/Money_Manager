import {useEffect, useMemo, useState} from "react";
import toast from "react-hot-toast";
import {Bell, Check, Crown, HandCoins, Link2, MessageSquare, Plus, ReceiptText, RefreshCcw, Sparkles, UploadCloud, UsersRound, WalletCards} from "lucide-react";
import {Client} from "@stomp/stompjs";
import Dashboard from "../components/Dashboard.jsx";
import PageHeader from "../components/PageHeader.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS, BASE_URL} from "../util/apiEndpoints.js";
import {useUser} from "../hooks/useUser.jsx";
import uploadProfileImage from "../util/uploadProfileImage.js";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const today = new Date().toISOString().slice(0, 10);
const money = (value) => `Rs ${Number(value || 0).toLocaleString("en-IN")}`;
const normalizeStatus = (status) => String(status || "active").toLowerCase();
const displayStatus = (status) => {
    const normalized = normalizeStatus(status);
    return normalized.charAt(0).toUpperCase() + normalized.slice(1);
};

const Friends = () => {
    const {user} = useUser();
    const queryClient = useQueryClient();
    const [friendForm, setFriendForm] = useState({name: "", email: "", phone: "", upiId: "", status: "active"});
    const [groupForm, setGroupForm] = useState({name: "", type: "Trip", icon: "TR", friendIds: []});
    const [expenseForm, setExpenseForm] = useState({title: "", amount: "", category: "Food", expenseDate: today, splitType: "equal", paidByFriendId: "", groupId: "", note: "", receiptUrl: ""});
    const [recurringForm, setRecurringForm] = useState({title: "", amount: "", category: "Rent", splitType: "equal", frequency: "monthly", nextDueDate: today, paidByFriendId: "", groupId: "", note: "", receiptUrl: ""});
    const [splitDraft, setSplitDraft] = useState({});
    const [settlementForm, setSettlementForm] = useState({friendId: "", amount: "", settlementDate: today, method: "UPI", direction: "friend_paid_you", note: ""});
    const [reminderForm, setReminderForm] = useState({friendId: "", message: "", amount: "", dueDate: today});
    const [commentForm, setCommentForm] = useState({expenseId: "", comment: ""});
    const [liveConnected, setLiveConnected] = useState(false);
    const [activeView, setActiveView] = useState("people");
    const {data = {friends: [], groups: [], expenses: [], settlements: [], reminders: [], activities: [], recurringExpenses: [], groupReports: [], settlementSuggestions: [], limits: {}}, isLoading: loading, refetch: loadData} = useQuery({
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
    const recurringExpenses = data.recurringExpenses || [];
    const groupReports = data.groupReports || [];
    const settlementSuggestions = data.settlementSuggestions || [];
    const limits = data.limits || {};
    const isPro = user?.planType === "PRO" || (data.planType || limits.planType) === "PRO";

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
        if (!isPro || !user?.id) {
            setLiveConnected(false);
            return;
        }
        const socketUrl = `${BASE_URL.replace(/^http/, "ws").replace(/\/$/, "")}/friend-live`;
        const client = new Client({
            brokerURL: socketUrl,
            reconnectDelay: 5000,
            onConnect: () => {
                setLiveConnected(true);
                client.subscribe(`/topic/friends/${user.id}`, () => invalidateFriendsCaches());
            },
            onWebSocketClose: () => setLiveConnected(false),
            onStompError: () => setLiveConnected(false),
        });
        client.activate();
        return () => {
            setLiveConnected(false);
            client.deactivate();
        };
    }, [isPro, user?.id]);

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

    const inviteFriend = async () => {
        if (!friendForm.name.trim()) return toast.error("Friend name is required");
        const response = await axiosConfig.post(API_ENDPOINTS.FRIEND_INVITE, friendForm);
        setFriendForm({name: "", email: "", phone: "", upiId: "", status: "active"});
        toast.success(`Invite created: ${response.data?.inviteCode || "ready"}`);
        invalidateFriendsCaches();
    };

    const updateInvite = async (friend, action) => {
        const endpoint = action === "accept" ? API_ENDPOINTS.FRIEND_INVITE_ACCEPT(friend.inviteCode) : API_ENDPOINTS.FRIEND_INVITE_REJECT(friend.inviteCode);
        await axiosConfig.post(endpoint);
        toast.success(action === "accept" ? "Invite accepted" : "Invite rejected");
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
        if (!isPro && expenseForm.splitType !== "equal") return toast.error("Advanced split types are Pro features");
        if (!isPro && expenseForm.receiptUrl) return toast.error("Receipt upload is a Pro feature");
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
        setExpenseForm({title: "", amount: "", category: "Food", expenseDate: today, splitType: "equal", paidByFriendId: "", groupId: "", note: "", receiptUrl: ""});
        setSplitDraft({});
        toast.success("Shared expense added");
        invalidateFriendsCaches();
    };

    const uploadReceipt = async (event) => {
        const file = event.target.files?.[0];
        if (!file) return;
        if (!isPro) return toast.error("Receipt upload is a Pro feature");
        const url = await uploadProfileImage(file);
        setExpenseForm((prev) => ({...prev, receiptUrl: url}));
        toast.success("Receipt uploaded");
    };

    const addRecurringExpense = async (event) => {
        event.preventDefault();
        if (!isPro) return toast.error("Recurring shared expenses are Pro features");
        if (!recurringForm.title || !recurringForm.amount) return toast.error("Title and amount are required");
        await axiosConfig.post(API_ENDPOINTS.FRIEND_RECURRING_EXPENSES, {
            ...recurringForm,
            amount: Number(recurringForm.amount),
            paidByFriendId: recurringForm.paidByFriendId || null,
            groupId: recurringForm.groupId || null,
            active: true,
        });
        setRecurringForm({title: "", amount: "", category: "Rent", splitType: "equal", frequency: "monthly", nextDueDate: today, paidByFriendId: "", groupId: "", note: "", receiptUrl: ""});
        toast.success("Recurring shared expense added");
        invalidateFriendsCaches();
    };

    const processRecurringDue = async () => {
        if (!isPro) return toast.error("Recurring shared expenses are Pro features");
        const response = await axiosConfig.post(API_ENDPOINTS.FRIEND_PROCESS_RECURRING_DUE);
        toast.success(`${response.data?.length || 0} due transactions created`);
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
                    action={
                        <div className="flex flex-wrap gap-2">
                            <span className={`add-btn ${isPro && liveConnected ? "border-[#d9ff72] text-[#d9ff72]" : ""}`}>
                                <Sparkles size={16} /> {isPro ? (liveConnected ? "Live connected" : "Connecting live") : "Manual refresh"}
                            </span>
                            <button onClick={loadData} className="add-btn"><RefreshCcw size={16} /> Refresh</button>
                        </div>
                    }
                />

                <section className="grid gap-4 md:grid-cols-4">
                    <Summary icon={WalletCards} label="Owed to you" value={money(data.totalOwedToYou)} tone="text-[#d9ff72]" />
                    <Summary icon={HandCoins} label="You owe" value={money(data.totalYouOwe)} tone="text-rose-300" />
                    <Summary icon={ReceiptText} label="Net balance" value={money(data.netBalance)} tone="text-white" />
                    <Summary icon={Crown} label="Plan" value={isPro ? "PRO" : "FREE"} tone={isPro ? "text-[#d9ff72]" : "text-white"} />
                </section>

                <ViewTabs
                    tabs={[
                        {id: "people", label: "People & groups"},
                        {id: "expenses", label: "Split expenses"},
                        {id: "settle", label: "Settle & insights"},
                    ]}
                    active={activeView}
                    onChange={setActiveView}
                />

                {activeView === "people" && <section className="grid gap-4 xl:grid-cols-[0.9fr_1.1fr]">
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
                            <div className="grid gap-2 md:grid-cols-2">
                                <button className="add-btn add-btn-fill"><Plus size={16} /> Add Friend</button>
                                <button type="button" onClick={inviteFriend} className="add-btn"><Link2 size={16} /> Create Invite</button>
                            </div>
                        </form>
                    </Panel>

                    <Panel title="Friend balances" action={`${friends.length} friends`}>
                        <div className="grid gap-3 md:grid-cols-2">
                            {friends.map((friend) => {
                                const status = normalizeStatus(friend.status);
                                const isActive = status === "active";
                                const isRejected = status === "rejected";
                                const isBlocked = status === "blocked";
                                return (
                                    <div key={friend.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                        <div className="flex items-start justify-between gap-3">
                                            <div>
                                                <p className="font-semibold text-white">{friend.name}</p>
                                                <p className="text-xs text-white/65">{friend.email || friend.phone || friend.inviteCode}</p>
                                            </div>
                                            <span className={`rounded-md px-2 py-1 text-xs ${isActive ? "bg-emerald-400/10 text-emerald-200" : "bg-white/10 text-white/78"}`}>{displayStatus(friend.status)}</span>
                                        </div>
                                        <p className={`mt-3 text-xl font-black ${Number(friend.balance) >= 0 ? "text-[#d9ff72]" : "text-rose-300"}`}>
                                            {Number(friend.balance) >= 0 ? `${friend.name} owes you ` : `You owe ${friend.name} `}{money(Math.abs(Number(friend.balance || 0)))}
                                        </p>
                                        {!isActive && friend.inviteCode && (
                                            <p className="mt-2 inline-flex rounded-md border border-white/14 bg-white/5 px-2 py-1 text-xs text-white/65">
                                                Invite code: {friend.inviteCode}
                                            </p>
                                        )}
                                        <div className="mt-3 flex flex-wrap gap-2">
                                            {!isActive && !isBlocked && <button className="dark-icon-btn px-3 text-xs" onClick={() => updateInvite(friend, "accept")}>Accept</button>}
                                            {!isRejected && !isBlocked && <button className="dark-icon-btn px-3 text-xs" onClick={() => updateInvite(friend, "reject")}>Reject</button>}
                                            {!isBlocked && <button className="dark-icon-btn px-3 text-xs" onClick={() => updateStatus(friend.id, "blocked")}>Block</button>}
                                        </div>
                                    </div>
                                );
                            })}
                            {!loading && friends.length === 0 && <Empty text="Add friends to start splitting expenses." />}
                        </div>
                    </Panel>
                </section>}

                {activeView === "expenses" && <section className="grid gap-4 xl:grid-cols-[1fr_0.9fr]">
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
                                    <option value="exact" disabled={!isPro}>Exact amount {isPro ? "" : "(Pro)"}</option>
                                    <option value="percentage" disabled={!isPro}>Percentage {isPro ? "" : "(Pro)"}</option>
                                    <option value="shares" disabled={!isPro}>Shares {isPro ? "" : "(Pro)"}</option>
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
                            <div className="grid gap-3 md:grid-cols-[1fr_auto]">
                                <input className="input-box" placeholder="Receipt URL (Pro)" value={expenseForm.receiptUrl} onChange={(e) => setExpenseForm({...expenseForm, receiptUrl: e.target.value})} disabled={!isPro} />
                                <label className={`add-btn cursor-pointer ${!isPro ? "opacity-50" : ""}`}>
                                    <UploadCloud size={16} /> Upload
                                    <input type="file" accept="image/*" className="hidden" onChange={uploadReceipt} disabled={!isPro} />
                                </label>
                            </div>
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
                </section>}

                {activeView === "settle" && <section className="grid gap-4 xl:grid-cols-3">
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
                </section>}

                {activeView === "settle" && <section className="grid gap-4 xl:grid-cols-[1fr_1fr]">
                    <Panel title="Recurring shared expense" action={isPro ? "Pro automation" : "Pro locked"}>
                        <form onSubmit={addRecurringExpense} className="grid gap-3">
                            <div className="grid gap-3 md:grid-cols-2">
                                <input className="input-box" placeholder="Monthly rent, Netflix..." value={recurringForm.title} onChange={(e) => setRecurringForm({...recurringForm, title: e.target.value})} disabled={!isPro} />
                                <input className="input-box" type="number" placeholder="Amount" value={recurringForm.amount} onChange={(e) => setRecurringForm({...recurringForm, amount: e.target.value})} disabled={!isPro} />
                            </div>
                            <div className="grid gap-3 md:grid-cols-3">
                                <select className="input-box" value={recurringForm.frequency} onChange={(e) => setRecurringForm({...recurringForm, frequency: e.target.value})} disabled={!isPro}>
                                    <option value="weekly">Weekly</option>
                                    <option value="monthly">Monthly</option>
                                    <option value="yearly">Yearly</option>
                                </select>
                                <input className="input-box" type="date" value={recurringForm.nextDueDate} onChange={(e) => setRecurringForm({...recurringForm, nextDueDate: e.target.value})} disabled={!isPro} />
                                <select className="input-box" value={recurringForm.groupId} onChange={(e) => setRecurringForm({...recurringForm, groupId: e.target.value})} disabled={!isPro}>
                                    <option value="">No group</option>
                                    {groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}
                                </select>
                            </div>
                            <div className="grid gap-2 md:grid-cols-2">
                                <button className="add-btn add-btn-fill" disabled={!isPro}><Sparkles size={16} /> Add Recurring</button>
                                <button type="button" onClick={processRecurringDue} className="add-btn" disabled={!isPro}>Create Due Transactions</button>
                            </div>
                        </form>
                        <div className="mt-4 space-y-2">
                            {recurringExpenses.map((item) => (
                                <div key={item.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-3">
                                    <p className="font-semibold text-white">{item.title}</p>
                                    <p className="text-xs text-white/65">{money(item.amount)} | {item.frequency} | due {item.nextDueDate}</p>
                                </div>
                            ))}
                        </div>
                    </Panel>

                    <Panel title="Pro limits and suggestions">
                        <div className="grid gap-3 md:grid-cols-2">
                            <div className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                <p className="text-xs uppercase text-white/55">Usage</p>
                                <p className="mt-2 text-sm text-white/80">Friends: {limits.friendsUsed || 0} / {limits.friendsLimit || 5}</p>
                                <p className="text-sm text-white/80">Groups: {limits.groupsUsed || 0} / {limits.groupsLimit || 2}</p>
                                <p className="text-sm text-white/80">Monthly splits: {limits.sharedExpensesThisMonth || 0} / {limits.sharedExpensesLimit || 20}</p>
                            </div>
                            <div className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                <p className="text-xs uppercase text-white/55">AI settlement ideas</p>
                                {settlementSuggestions.slice(0, 3).map((suggestion, index) => (
                                    <p key={`${suggestion.friendId}-${index}`} className="mt-2 text-sm text-white/80">{suggestion.fromName} pays {suggestion.toName} {money(suggestion.amount)}</p>
                                ))}
                                {!isPro && <p className="mt-2 text-sm text-white/65">Upgrade to Pro to unlock smart settlement suggestions.</p>}
                            </div>
                        </div>
                    </Panel>
                </section>}

                {activeView === "people" && <section className="grid gap-4 xl:grid-cols-2">
                    <Panel title="Groups and roles" action={`${groups.length} groups`}>
                        <div className="space-y-3">
                            {groups.map((group) => (
                                <div key={group.id} className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                    <p className="font-semibold text-white">{group.icon} {group.name}</p>
                                    <div className="mt-3 flex flex-wrap gap-2">
                                        {(group.members || []).map((member) => <span key={member.friendId} className="rounded-md bg-black/25 px-2 py-1 text-xs text-white/72">{member.friendName}: {member.role}</span>)}
                                    </div>
                                </div>
                            ))}
                            {groups.length === 0 && <Empty text="Create a group for trips, roommates, family, or office lunch." />}
                        </div>
                    </Panel>

                    <Panel title="Group reports" action={isPro ? "Pro analytics" : "Pro locked"}>
                        <div className="space-y-3">
                            {groupReports.map((report) => (
                                <div key={report.groupId} className="rounded-lg border border-white/14 bg-white/[0.03] p-4">
                                    <div className="flex items-start justify-between gap-3">
                                        <div>
                                            <p className="font-semibold text-white">{report.groupName}</p>
                                            <p className="text-xs text-white/65">{report.expenseCount} expenses | {report.memberCount} members</p>
                                        </div>
                                        <p className="font-black text-[#d9ff72]">{money(report.totalSpent)}</p>
                                    </div>
                                    <p className="mt-3 text-sm text-white/75">Top category: {report.topCategory} | Top payer: {report.topPayerName}</p>
                                </div>
                            ))}
                            {groupReports.length === 0 && <Empty text={isPro ? "Group reports appear after group expenses." : "Upgrade to Pro to unlock group reports."} />}
                        </div>
                    </Panel>
                </section>}

                {activeView === "expenses" && <section className="grid gap-4 xl:grid-cols-[1.2fr_0.8fr]">
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
                </section>}
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

const ViewTabs = ({tabs, active, onChange}) => (
    <div className="sticky top-[76px] z-10 rounded-lg border border-white/14 bg-[#172119]/95 p-1.5 shadow-xl shadow-black/20 backdrop-blur">
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

