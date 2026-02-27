import { useState, useEffect } from "react";
import EmojiPickerPopup from "./EmojiPickerPopup.jsx";
import Input from "./Input.jsx";
const AddExpenseForm = ({ onAddExpense, categories }) => {
    const [expense, setExpense] = useState({
        name: "",
        categoryId: "",
        amount: "",
        date: "",
        icon: "",
        paymentMethod: "UPI",
        notes: "",
        tags: "",
        receiptUrl: "",
    });
    useEffect(() => {
        if (categories && categories.length > 0 && !expense.categoryId) {
          
            setExpense((prev) => ({ ...prev, categoryId: categories[0].id })); 
        }
    }, [categories, expense.categoryId]);

    const handleChange = (key, value) => setExpense({ ...expense, [key]: value }); 
    const categoryOptions = categories.map((cat) => ({
        value: cat.id, // Correct for MySQL 'id'
        label: `${cat.name}`, // Display icon and name in dropdown
    }));

    return (
        <div>
            <EmojiPickerPopup
                icon={expense.icon} 
                onSelect={(selectedIcon) => handleChange("icon", selectedIcon)}
            />

            <Input
                value={expense.name}
                onChange={({ target }) => handleChange("name", target.value)}
                label="Income Source"
                placeholder="e.g., Electricity, Wifi"
                type="text"
            />
            <Input
                label="Category"
                value={expense.categoryId}
                onChange={({ target }) => handleChange("categoryId", target.value)}
                isSelect={true}
                options={categoryOptions}
            />

            <Input
                value={expense.amount}
                onChange={({ target }) => handleChange("amount", target.value)}
                label="Amount"
                placeholder="e.g., 150.00"
                type="number"
            />

            <Input
                value={expense.date}
                onChange={({ target }) => handleChange("date", target.value)}
                label="Date"
                placeholder=""
                type="date"
            />

            <Input
                label="Payment Method"
                value={expense.paymentMethod}
                onChange={({ target }) => handleChange("paymentMethod", target.value)}
                isSelect={true}
                options={[
                    {value: "cash", label: "Cash"},
                    {value: "UPI", label: "UPI"},
                    {value: "card", label: "Card"},
                    {value: "bank", label: "Bank"},
                ]}
            />

            <Input
                value={expense.tags}
                onChange={({ target }) => handleChange("tags", target.value)}
                label="Tags"
                placeholder="e.g., monthly, family"
                type="text"
            />

            <Input
                value={expense.receiptUrl}
                onChange={({ target }) => handleChange("receiptUrl", target.value)}
                label="Receipt URL"
                placeholder="Paste receipt link"
                type="text"
            />

            <textarea
                value={expense.notes}
                onChange={({ target }) => handleChange("notes", target.value)}
                className="input-box min-h-24"
                placeholder="Notes"
            />

            <div className="flex justify-end mt-6">
                <button
                    type="button"
                    className="add-btn add-btn-fill"
                    onClick={() => onAddExpense(expense)} 
                >
                    Add Expense
                </button>
            </div>
        </div>
    );
};

export default AddExpenseForm;
