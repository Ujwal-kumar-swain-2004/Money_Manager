import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import {useState} from "react";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";
import IncomeList from "../components/IncomeList.jsx";
import Modal from "../components/Modal.jsx";
import {Plus} from "lucide-react";
import AddIncomeForm from "../components/AddIncomeForm.jsx";
import DeleteAlert from "../components/DeleteAlert.jsx";
import IncomeOverview from "../components/IncomeOverview.jsx";
import PageHeader from "../components/PageHeader.jsx";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const Income = () => {
    useUser();
    const queryClient = useQueryClient();

    const [openAddIncomeModal, setOpenAddIncomeModal] = useState(false);
    const [openDeleteAlert, setOpenDeleteAlert] = useState({
        show: false,
        data: null,
    });
    const {data: incomeData = []} = useQuery({
        queryKey: queryKeys.incomes,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_INCOMES);
            return response.data || [];
        },
        staleTime: cacheTimes.transactions,
        onError: (error) => {
            console.error('Failed to fetch income details:', error);
            toast.error(error.response?.data?.message || "Failed to fetch income details");
        },
    });

    const {data: categories = []} = useQuery({
        queryKey: queryKeys.categoryType("income"),
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.CATEGORY_BY_TYPE("income"));
            return response.data || [];
        },
        staleTime: cacheTimes.categories,
    });

    const {data: families = []} = useQuery({
        queryKey: queryKeys.families,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.FAMILIES);
            return response.data || [];
        },
        staleTime: cacheTimes.family,
    });

    const familyId = families[0]?.id;
    const {data: familyMembers = []} = useQuery({
        queryKey: queryKeys.familyMembers(familyId),
        enabled: !!familyId,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.FAMILY_MEMBERS(familyId));
            return response.data || [];
        },
        staleTime: cacheTimes.family,
    });

    const invalidateIncomeCaches = () => {
        queryClient.invalidateQueries({queryKey: queryKeys.incomes});
        queryClient.invalidateQueries({queryKey: queryKeys.dashboard});
        queryClient.invalidateQueries({queryKey: ["money-plan"]});
        queryClient.invalidateQueries({queryKey: queryKeys.aiInsights});
        if (familyId) queryClient.invalidateQueries({queryKey: queryKeys.familyDashboard(familyId)});
    };

    const handleAddIncome = async (income) => {
        const {name, amount, date, icon, categoryId, paymentMethod, notes, tags, receiptUrl, familyMemberId} = income;

        if (!name.trim()) {
            toast.error("Please enter a name");
            return;
        }

        if (!amount || isNaN(amount) || Number(amount) <= 0) {
            toast.error("Amount should be a valid number greater than 0");
            return;
        }

        if (!date) {
            toast.error("Please select a date");
            return;
        }

        const today = new Date().toISOString().split('T')[0];
        if (date > today) {
            toast.error('Date cannot be in the future');
            return;
        }

        if (!categoryId) {
            toast.error("Please select a category");
            return;
        }

        try {
            const response = await axiosConfig.post(API_ENDPOINTS.ADD_INCOME, {
                name,
                amount: Number(amount),
                date,
                icon,
                categoryId,
                paymentMethod,
                notes,
                tags,
                receiptUrl,
                familyMemberId: familyMemberId || null,
            })
            if (response.status === 201) {
                setOpenAddIncomeModal(false);
                toast.success("Income added successfully");
                invalidateIncomeCaches();
            }
        }catch(error){
            console.log('Error adding income', error);
            toast.error(error.response?.data?.message || "Failed to adding income");
        }
    }

    //delete income details
    const deleteIncome = async (id) => {
        try {
            await axiosConfig.delete(API_ENDPOINTS.DELETE_INCOME(id));
            setOpenDeleteAlert({show: false, data: null});
            toast.success("Income deleted successfully");
            invalidateIncomeCaches();
        }catch(error) {
            console.log('Error deleting income', error);
            toast.error(error.response?.data?.message || "Failed to delete income");
        }
    }

    const handleDownloadIncomeDetails = async() => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.INCOME_EXCEL_DOWNLOAD, {responseType: "blob"});
            let filename = "income_details.xlsx";
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", filename);
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
            window.URL.revokeObjectURL(url);
            toast.success("Download income details successfully");
        }catch(error) {
            console.error('Error downloading income details:', error);
            toast.error(error.response?.data?.message || "Failed to download income");
        }
    }

    const handleEmailIncomeDetails = async () => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.EMAIL_INCOME);
            if (response.status === 200) {
                toast.success("Income details emailed successfully");
            }
        }catch(error) {
            console.error('Error emailing income details:', error);
            toast.error(error.response?.data?.message || "Failed to email income");
        }
    }

    return (
        <Dashboard activeMenu="Income">
            <div className="mx-auto max-w-7xl">
                <PageHeader
                    eyebrow="Cash in"
                    title="Income"
                    description="Track salary, freelance work, refunds, bonuses, and every source that grows your balance."
                    action={<button onClick={() => setOpenAddIncomeModal(true)} className="add-btn"><Plus size={16} /> Add Income</button>}
                />
                <div className="grid grid-cols-1 gap-6">
                    <div>
                        {/* overview for income with line char */}
                        <IncomeOverview transactions={incomeData} onAddIncome={() => setOpenAddIncomeModal(true)} />
                    </div>

                    <IncomeList
                        transactions={incomeData}
                        onDelete={(id) => setOpenDeleteAlert({show: true, data: id})}
                        onDownload={handleDownloadIncomeDetails}
                        onEmail={handleEmailIncomeDetails}
                    />

                    {/* Add Income Modal */}
                    <Modal
                        isOpen={openAddIncomeModal}
                        onClose={() => setOpenAddIncomeModal(false)}
                        title="Add Income"
                    >
                        <AddIncomeForm
                            onAddIncome={(income) => handleAddIncome(income)}
                            categories={categories}
                            familyMembers={familyMembers}
                        />
                    </Modal>

                    {/* Delete Income Modal */}
                    <Modal
                        isOpen={openDeleteAlert.show}
                        onClose={() => setOpenDeleteAlert({show: false, data: null})}
                        title="Delete Income"
                    >
                        <DeleteAlert
                            content="Are you sure want to delete this income details?"
                            onDelete={() => deleteIncome(openDeleteAlert.data)}
                        />
                    </Modal>
                </div>
            </div>
        </Dashboard>
    )
}

export default Income;

