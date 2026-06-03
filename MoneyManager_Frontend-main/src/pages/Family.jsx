import {useEffect, useMemo, useState} from "react";
import toast from "react-hot-toast";
import {ArrowRightLeft, Landmark, Plus, RefreshCcw, UserPlus, UsersRound, WalletCards} from "lucide-react";
import Dashboard from "../components/Dashboard.jsx";
import PageHeader from "../components/PageHeader.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import {useUser} from "../hooks/useUser.jsx";

const currency = (value) => `₹${Number(value || 0).toLocaleString("en-IN")}`;
const today = new Date().toISOString().split("T")[0];

const Family = () => {
    useUser();
    const [families, setFamilies] = useState([]);
    const [activeFamilyId, setActiveFamilyId] = useState("");
    const [dashboard, setDashboard] = useState(null);
    const [familyName, setFamilyName] = useState("My Family");
    const [memberForm, setMemberForm] = useState({name: "", role: "Child", monthlyAllowance: ""});
    const [transferForm, setTransferForm] = useState({fromMemberId: "", toMemberId: "", amount: "", transferDate: today, note: ""});
    const [loading, setLoading] = useState(false);

    const members = useMemo(() => dashboard?.members || [], [dashboard]);
    const transfers = dashboard?.transfers || [];

    const loadFamilies = async () => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.FAMILIES);
            const list = response.data || [];
            setFamilies(list);
            if (list.length > 0) {
                setActiveFamilyId((current) => current || list[0].id);
            }
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to load families");
        }
    };

    const loadDashboard = async (familyId = activeFamilyId) => {
        if (!familyId) return;
        try {
            setLoading(true);
            const response = await axiosConfig.get(API_ENDPOINTS.FAMILY_DASHBOARD(familyId));
            setDashboard(response.data);
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to load family dashboard");
        } finally {
            setLoading(false);
        }
    };

    const createFamily = async () => {
        if (!familyName.trim()) {
            toast.error("Family name is required");
            return;
        }
        try {
            const response = await axiosConfig.post(API_ENDPOINTS.FAMILIES, {name: familyName});
            setFamilies([response.data]);
            setActiveFamilyId(response.data.id);
            setDashboard(response.data);
            toast.success("Family space created");
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to create family");
        }
    };

    const addMember = async () => {
        if (!memberForm.name.trim()) {
            toast.error("Member name is required");
            return;
        }
        try {
            await axiosConfig.post(API_ENDPOINTS.FAMILY_MEMBERS(activeFamilyId), {
                ...memberForm,
                monthlyAllowance: Number(memberForm.monthlyAllowance || 0),
            });
            setMemberForm({name: "", role: "Child", monthlyAllowance: ""});
            await loadDashboard();
            toast.success("Family member added");
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to add member");
        }
    };

    const addTransfer = async () => {
        if (!transferForm.toMemberId || !transferForm.amount || Number(transferForm.amount) <= 0) {
            toast.error("Choose receiver and amount");
            return;
        }
        try {
            await axiosConfig.post(API_ENDPOINTS.FAMILY_TRANSFERS(activeFamilyId), {
                ...transferForm,
                fromMemberId: transferForm.fromMemberId || null,
                amount: Number(transferForm.amount),
            });
            setTransferForm({fromMemberId: "", toMemberId: "", amount: "", transferDate: today, note: ""});
            await loadDashboard();
            toast.success("Money transfer recorded");
        } catch (error) {
            toast.error(error.response?.data?.message || "Failed to record transfer");
        }
    };

    useEffect(() => {
        loadFamilies();
    }, []);

    useEffect(() => {
        loadDashboard(activeFamilyId);
    }, [activeFamilyId]);

    useEffect(() => {
        if (members.length > 0 && !transferForm.toMemberId) {
            setTransferForm((prev) => ({...prev, toMemberId: members[0].id}));
        }
    }, [members, transferForm.toMemberId]);

    return (
        <Dashboard activeMenu="Family">
            <div className="mx-auto max-w-7xl space-y-6">
                <PageHeader
                    eyebrow="Household ledger"
                    title="Family Money"
                    description="Maintain allowances, family transfers, child spending, shared bills, and who used the money."
                    action={<button onClick={() => loadDashboard()} className="add-btn"><RefreshCcw size={16} /> Refresh</button>}
                />

                {families.length === 0 ? (
                    <section className="rounded-lg border border-white/10 bg-[#101914] p-6 shadow-sm">
                        <div className="flex items-center gap-3 text-white">
                            <div className="grid h-11 w-11 place-items-center rounded-md bg-[#d9ff72] text-[#101914]">
                                <UsersRound size={22} />
                            </div>
                            <div>
                                <h2 className="text-xl font-semibold">Create your family space</h2>
                                <p className="text-sm text-white/55">Start with one household, then add father, mother, son, daughter, or roommates.</p>
                            </div>
                        </div>
                        <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                            <input className="input-box" value={familyName} onChange={({target}) => setFamilyName(target.value)} placeholder="Family name" />
                            <button className="add-btn add-btn-fill" onClick={createFamily}><Plus size={16} /> Create Family</button>
                        </div>
                    </section>
                ) : (
                    <>
                        <section className="grid grid-cols-1 gap-4 lg:grid-cols-[1.5fr_1fr]">
                            <div className="rounded-lg border border-white/10 bg-[#101914] p-6 text-white">
                                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                                    <div>
                                        <p className="text-sm font-semibold text-[#d9ff72]">Family overview</p>
                                        <h2 className="mt-2 text-3xl font-black">{dashboard?.name || "Family Money"}</h2>
                                        <p className="mt-2 text-sm text-white/55">Invite code: {dashboard?.inviteCode || "loading"}</p>
                                    </div>
                                    <select className="input-box max-w-xs" value={activeFamilyId} onChange={({target}) => setActiveFamilyId(target.value)}>
                                        {families.map((family) => <option key={family.id} value={family.id}>{family.name}</option>)}
                                    </select>
                                </div>
                                <div className="mt-6 grid grid-cols-1 gap-3 md:grid-cols-3">
                                    <SummaryCard icon={Landmark} label="Family income" value={currency(dashboard?.totalIncome)} />
                                    <SummaryCard icon={WalletCards} label="Family expense" value={currency(dashboard?.totalExpense)} />
                                    <SummaryCard icon={ArrowRightLeft} label="Net balance" value={currency(dashboard?.totalBalance)} />
                                </div>
                            </div>

                            <div className="rounded-lg border border-white/10 bg-[#131c17] p-5">
                                <h3 className="flex items-center gap-2 text-lg font-bold text-white"><UserPlus size={18} /> Add member</h3>
                                <div className="mt-4 grid gap-3">
                                    <input className="input-box" value={memberForm.name} onChange={({target}) => setMemberForm({...memberForm, name: target.value})} placeholder="Name, e.g. Son" />
                                    <div className="grid grid-cols-2 gap-3">
                                        <select className="input-box" value={memberForm.role} onChange={({target}) => setMemberForm({...memberForm, role: target.value})}>
                                            <option>Parent</option>
                                            <option>Child</option>
                                            <option>Spouse</option>
                                            <option>Member</option>
                                        </select>
                                        <input className="input-box" value={memberForm.monthlyAllowance} onChange={({target}) => setMemberForm({...memberForm, monthlyAllowance: target.value})} type="number" placeholder="Allowance" />
                                    </div>
                                    <button className="add-btn add-btn-fill" onClick={addMember}><Plus size={16} /> Add Member</button>
                                </div>
                            </div>
                        </section>

                        <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
                            {members.map((member) => (
                                <div key={member.id} className="rounded-lg border border-white/10 bg-[#111a15] p-5 text-white">
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <p className="text-sm text-white/50">{member.role}</p>
                                            <h3 className="text-xl font-bold">{member.name}</h3>
                                        </div>
                                        <div className="grid h-10 w-10 place-items-center rounded-md bg-white/10">
                                            <UsersRound size={18} />
                                        </div>
                                    </div>
                                    <div className="mt-5 space-y-3 text-sm">
                                        <Bar label="Allowance" value={currency(member.monthlyAllowance)} />
                                        <Bar label="Received" value={currency(member.monthReceived)} />
                                        <Bar label="Spent" value={currency(member.monthSpent)} danger />
                                        <Bar label="Remaining" value={currency(member.allowanceRemaining)} highlight />
                                    </div>
                                </div>
                            ))}
                            {!loading && members.length === 0 && <p className="text-white/60">Add members to start tracking family spending.</p>}
                        </section>

                        <section className="grid grid-cols-1 gap-4 lg:grid-cols-[0.9fr_1.1fr]">
                            <div className="rounded-lg border border-white/10 bg-[#131c17] p-5">
                                <h3 className="text-lg font-bold text-white">Record family transfer</h3>
                                <div className="mt-4 grid gap-3">
                                    <select className="input-box" value={transferForm.fromMemberId} onChange={({target}) => setTransferForm({...transferForm, fromMemberId: target.value})}>
                                        <option value="">Household wallet</option>
                                        {members.map((member) => <option key={member.id} value={member.id}>{member.name}</option>)}
                                    </select>
                                    <select className="input-box" value={transferForm.toMemberId} onChange={({target}) => setTransferForm({...transferForm, toMemberId: target.value})}>
                                        {members.map((member) => <option key={member.id} value={member.id}>{member.name}</option>)}
                                    </select>
                                    <div className="grid grid-cols-2 gap-3">
                                        <input className="input-box" value={transferForm.amount} onChange={({target}) => setTransferForm({...transferForm, amount: target.value})} type="number" placeholder="Amount" />
                                        <input className="input-box" value={transferForm.transferDate} onChange={({target}) => setTransferForm({...transferForm, transferDate: target.value})} type="date" />
                                    </div>
                                    <input className="input-box" value={transferForm.note} onChange={({target}) => setTransferForm({...transferForm, note: target.value})} placeholder="Note, e.g. School trip money" />
                                    <button className="add-btn add-btn-fill" onClick={addTransfer}><ArrowRightLeft size={16} /> Save Transfer</button>
                                </div>
                            </div>

                            <div className="rounded-lg border border-white/10 bg-[#101914] p-5">
                                <h3 className="text-lg font-bold text-white">Recent family movement</h3>
                                <div className="mt-4 divide-y divide-white/10">
                                    {transfers.map((transfer) => (
                                        <div key={transfer.id} className="flex items-center justify-between gap-4 py-4">
                                            <div>
                                                <p className="font-semibold text-white">{transfer.fromMemberName || "Household wallet"} → {transfer.toMemberName}</p>
                                                <p className="text-sm text-white/45">{transfer.transferDate} {transfer.note ? `· ${transfer.note}` : ""}</p>
                                            </div>
                                            <p className="text-lg font-black text-[#d9ff72]">{currency(transfer.amount)}</p>
                                        </div>
                                    ))}
                                    {transfers.length === 0 && <p className="py-5 text-sm text-white/50">No family transfers recorded yet.</p>}
                                </div>
                            </div>
                        </section>
                    </>
                )}
            </div>
        </Dashboard>
    );
};

const SummaryCard = ({icon: Icon, label, value}) => (
    <div className="rounded-md border border-white/10 bg-white/[0.04] p-4">
        <Icon className="text-[#d9ff72]" size={20} />
        <p className="mt-4 text-xs uppercase text-white/45">{label}</p>
        <p className="mt-1 text-2xl font-black">{value}</p>
    </div>
);

const Bar = ({label, value, danger, highlight}) => (
    <div className="flex items-center justify-between">
        <span className="text-white/45">{label}</span>
        <span className={`${danger ? "text-red-300" : highlight ? "text-[#d9ff72]" : "text-white"} font-bold`}>{value}</span>
    </div>
);

export default Family;
