import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import {Search} from "lucide-react";
import {useState} from "react";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";
import TransactionInfoCard from "../components/TransactionInfoCard.jsx";
import moment from "moment";
import PageHeader from "../components/PageHeader.jsx";
import EmptyState from "../components/EmptyState.jsx";

const Filter = () => {
    useUser();
    const [type, setType] = useState("income");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [keyword, setKeyword] = useState("");
    const [sortField, setSortField] = useState("date");
    const [sortOrder, setSortOrder] = useState("asc");
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleSearch = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const response = await axiosConfig.post(API_ENDPOINTS.APPLY_FILTERS, {
                type,
                startDate,
                endDate,
                keyword,
                sortField,
                sortOrder
            });
            console.log('transactions: ', response.data);
            setTransactions(response.data);
        }catch (error) {
            console.error('Failed to fetch transactions: ', error);
            toast.error(error.message || "Failed to fetch transactions. Please try again.");
        }finally {
            setLoading(false);
        }

    }

    return (
        <Dashboard activeMenu="Filters">
            <div className="mx-auto max-w-7xl">
                <PageHeader
                    eyebrow="Search"
                    title="Transaction Explorer"
                    description="Find exactly what happened across income and expense history with date, keyword, and sort controls."
                />
                <div className="panel mb-5 p-5">
                    <div className="flex items-center justify-between mb-4">
                        <h5 className="section-title">Filters</h5>
                    </div>
                    <form className="grid grid-cols-1 gap-4 sm:grid-cols-3 md:grid-cols-6">
                        <div>
                            <label className="label" htmlFor="type">Type</label>
                            <select value={type} id="type" className="field mt-1" onChange={e => setType(e.target.value)}>
                                <option value="income">Income</option>
                                <option value="expense">Expense</option>
                            </select>
                        </div>
                        <div>
                            <label htmlFor="startdate" className="label">Start Date</label>
                            <input value={startDate} id="startdate" type="date" className="field mt-1" onChange={e => setStartDate(e.target.value)}/>
                        </div>
                        <div>
                            <label htmlFor="enddate" className="label">End Date</label>
                            <input value={endDate} id="enddate" type="date" className="field mt-1" onChange={e => setEndDate(e.target.value)}/>
                        </div>
                        <div>
                            <label htmlFor="sortfield" className="label">Sort Field</label>
                            <select value={sortField} id="sortfield" className="field mt-1" onChange={e => setSortField(e.target.value)}>
                                <option value="date">Date</option>
                                <option value="amount">Amount</option>
                                <option value="category">Category</option>
                            </select>
                        </div>
                        <div>
                            <label htmlFor="sortorder" className="label">Sort Order</label>
                            <select value={sortOrder} id="sortorder" className="field mt-1" onChange={e => setSortOrder(e.target.value)}>
                                <option value="asc">Ascending</option>
                                <option value="desc">Descending</option>
                            </select>
                        </div>
                        <div className="sm:col-span-1 md:col-span-1 flex items-end">
                            <div className="w-full">
                                <label htmlFor="keyword" className="label">Search</label>
                                <input value={keyword} id="keyword" type="text" placeholder="Search..." className="field mt-1" onChange={e => setKeyword(e.target.value)} />
                            </div>
                            <button onClick={handleSearch} className="mb-1 ml-2 flex h-10 w-10 items-center justify-center rounded-md bg-[#d9ff72] text-[#1f2a24]">
                                <Search size={20} />
                            </button>
                        </div>
                    </form>
                </div>
                <div className="panel p-5">
                    <div className="flex items-center justify-between mb-4">
                        <h5 className="section-title">Transactions</h5>
                    </div>
                    {transactions.length === 0 && !loading? (
                        <EmptyState title="No filtered results" description="Select filters and run a search to inspect transactions." />
                    ): ""}
                    {loading ? (
                        <p className="text-sm text-white/70">Loading transactions...</p>
                    ): ("")}
                    <div className="grid gap-3 md:grid-cols-2">
                    {transactions.map((transaction) => (
                        <TransactionInfoCard
                            key={transaction.id}
                            title={transaction.name}
                            icon={transaction.icon}
                            date={moment(transaction.date).format('Do MMM YYYY')}
                            amount={transaction.amount}
                            type={type}
                            hideDeleteBtn
                        />
                    ))}
                    </div>
                </div>
            </div>
        </Dashboard>
    )
}

export default Filter;

