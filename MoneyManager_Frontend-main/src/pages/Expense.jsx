import { useState } from "react";
import toast from "react-hot-toast";
import {useUser} from "../hooks/useUser.jsx";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import Dashboard from "../components/Dashboard.jsx";
import ExpenseOverview from "../components/ExpenseOverview.jsx";
import ExpenseList from "../components/ExpenseList.jsx";
import Modal from "../components/Modal.jsx";
import AddExpenseForm from "../components/AddExpenseForm.jsx";
import DeleteAlert from "../components/DeleteAlert.jsx";
import PageHeader from "../components/PageHeader.jsx";
import {Plus} from "lucide-react";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const Expense = () => {
    useUser();
    const queryClient = useQueryClient();
    const [openAddExpenseModal, setOpenAddExpenseModal] = useState(false);
    const [openDeleteAlert, setOpenDeleteAlert] = useState({
        show: false,
        data: null,
    });

    const {data: expenseData = []} = useQuery({
        queryKey: queryKeys.expenses,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_EXPENSE);
            return response.data || [];
        },
        staleTime: cacheTimes.transactions,
        onError: (error) => {
            console.error("Failed to fetch expense details:", error);
            toast.error("Failed to fetch expense details.");
        },
    });

    const {data: categories = []} = useQuery({
        queryKey: queryKeys.categoryType("expense"),
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.CATEGORY_BY_TYPE("expense"));
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

    const invalidateExpenseCaches = () => {
        queryClient.invalidateQueries({queryKey: queryKeys.expenses});
        queryClient.invalidateQueries({queryKey: queryKeys.dashboard});
        queryClient.invalidateQueries({queryKey: ["money-plan"]});
        queryClient.invalidateQueries({queryKey: queryKeys.aiInsights});
        if (familyId) queryClient.invalidateQueries({queryKey: queryKeys.familyDashboard(familyId)});
    };

    const handleAddExpense = async (expense) => {
        const { name, categoryId, amount, date, icon, paymentMethod, notes, tags, receiptUrl, familyMemberId } = expense; 

        if (!name.trim()) {
            toast.error("Name is required.");
            return;
        }

        if (!categoryId) { 
            toast.error("Category is required.");
            return;
        }

        if (!amount || isNaN(amount) || Number(amount) <= 0) {
            toast.error("Amount should be a valid number greater than 0.");
            return;
        }

        if (!date) {
            toast.error("Date is required.");
            return;
        }

        const today = new Date().toISOString().split('T')[0];
        if (date > today) {
            toast.error('Date cannot be in the future');
            return;
        }

        try {
            await axiosConfig.post(API_ENDPOINTS.ADD_EXPENSE, {
                name,
                categoryId, 
                amount: Number(amount), 
                date,
                icon,
                paymentMethod,
                notes,
                tags,
                receiptUrl,
                familyMemberId: familyMemberId || null,
            });

            setOpenAddExpenseModal(false);
            toast.success("Expense added successfully");
            invalidateExpenseCaches();
        } catch (error) {
            console.error(
                "Error adding expense:",
                error.response?.data?.message || error.message
            );
            toast.error(error.response?.data?.message || "Failed to add expense.");
        }
    };

    const deleteExpense = async (id) => {
        try {
            await axiosConfig.delete(API_ENDPOINTS.DELETE_EXPENSE(id));

            setOpenDeleteAlert({ show: false, data: null });
            toast.success("Expense details deleted successfully");
            invalidateExpenseCaches();
        } catch (error) {
            console.error(
                "Error deleting expense:",
                error.response?.data?.message || error.message
            );
            toast.error(error.response?.data?.message || "Failed to delete expense.");
        }
    };

    const handleDownloadExpenseDetails = async () => {
        try {
            const response = await axiosConfig.get(
                API_ENDPOINTS.EXPENSE_EXCEL_DOWNLOAD, 
                {
                    responseType: "blob", 
                }
            );

            let filename = "expense_details.xlsx"; 

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", filename); 
            document.body.appendChild(link);
            link.click(); 
            link.parentNode.removeChild(link); 
            window.URL.revokeObjectURL(url); 

            toast.success("Expense details downloaded successfully!");
        } catch (error) {
            console.error("Error downloading expense details:", error);
            toast.error("Failed to download expense details. Please try again.");
        }
    };

    const handleEmailExpenseDetails = async () => {
        try {
            const response = await axiosConfig.get(API_ENDPOINTS.EMAIL_EXPENSE);
            if(response.status === 200) {
                toast.success("Email sent");
            }
        }catch (e) {
            console.error("Error emailing expense details:", e);
            toast.error("Failed to email expense details. Please try again.");
        }
    }

    return (
        <Dashboard activeMenu="Expense">
            <div className="mx-auto max-w-7xl">
                <PageHeader
                    eyebrow="Cash out"
                    title="Expenses"
                    description="Record spending with payment method, notes, tags, and receipts so your money story stays searchable."
                    action={<button onClick={() => setOpenAddExpenseModal(true)} className="add-btn"><Plus size={16} /> Add Expense</button>}
                />
                <div className="grid grid-cols-1 gap-6">
                    <div className="">
                        <ExpenseOverview
                            transactions={expenseData}
                            onExpenseIncome={() => setOpenAddExpenseModal(true)}
                        />
                    </div>

                    <ExpenseList
                        transactions={expenseData}
                        onDelete={(id) => {
                            setOpenDeleteAlert({ show: true, data: id });
                        }}
                        onDownload={handleDownloadExpenseDetails}
                        onEmail={handleEmailExpenseDetails}
                    />

                    <Modal
                        isOpen={openAddExpenseModal}
                        onClose={() => setOpenAddExpenseModal(false)}
                        title="Add Expense"
                    >
                        <AddExpenseForm
                            onAddExpense={handleAddExpense}
                            categories={categories} 
                            familyMembers={familyMembers}
                        />
                    </Modal>

                    <Modal
                        isOpen={openDeleteAlert.show}
                        onClose={() => setOpenDeleteAlert({ show: false, data: null })}
                        title="Delete Expense"
                    >
                        <DeleteAlert
                            content="Are you sure you want to delete this expense detail?"
                            onDelete={() => deleteExpense(openDeleteAlert.data)}
                        />
                    </Modal>
                </div>
            </div>
        </Dashboard>
    );
};

export default Expense;

