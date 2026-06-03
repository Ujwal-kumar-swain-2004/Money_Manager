import {useEffect, useState} from "react";
import EmojiPickerPopup from "./EmojiPickerPopup.jsx";
import Input from "./Input.jsx";
import {LoaderCircle} from "lucide-react";

const AddIncomeForm = ({onAddIncome, categories, familyMembers = []}) => {
    const [income, setIncome] = useState({
        name: '',
        amount: '',
        date: '',
        icon: '',
        categoryId: '',
        paymentMethod: 'bank',
        notes: '',
        tags: '',
        receiptUrl: '',
        familyMemberId: '',
    })
    const [loading, setLoading] = useState(false);

    const categoryOptions = categories.map(category => ({
        value: category.id,
        label: category.name
    }))

    const handleChange = (key, value) => {
        setIncome({...income, [key]: value});
    }

    const handleAddIncome = async () => {
        setLoading(true);
        try {
            await onAddIncome(income);
        }finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (categories.length > 0 && !income.categoryId) {
            setIncome((prev) => ({...prev, categoryId: categories[0].id}))
        }
    }, [categories, income.categoryId]);

    return (
        <div>
            <EmojiPickerPopup
                icon={income.icon}
                onSelect={(selectedIcon) => handleChange('icon', selectedIcon)}
            />

            <Input
                value={income.name}
                onChange={({target}) => handleChange('name', target.value)}
                label="Income Source"
                placeholder="e.g., Salary, Freelance, Bonus"
                type="text"
            />

            <Input
                label="Category"
                value={income.categoryId}
                onChange={({target}) => handleChange('categoryId', target.value)}
                isSelect={true}
                options={categoryOptions}
            />

            <Input
                value={income.amount}
                onChange={({target}) => handleChange('amount', target.value)}
                label="Amount"
                placeholder="e.g., 500.00"
                type="number"
            />

            <Input
                value={income.date}
                onChange={({target}) => handleChange('date', target.value)}
                label="Date"
                placeholder=""
                type="date"
            />

            <Input
                label="Payment Method"
                value={income.paymentMethod}
                onChange={({target}) => handleChange('paymentMethod', target.value)}
                isSelect={true}
                options={[
                    {value: "cash", label: "Cash"},
                    {value: "UPI", label: "UPI"},
                    {value: "card", label: "Card"},
                    {value: "bank", label: "Bank"},
                ]}
            />

            {familyMembers.length > 0 && (
                <Input
                    label="Family Member"
                    value={income.familyMemberId}
                    onChange={({target}) => handleChange('familyMemberId', target.value)}
                    isSelect={true}
                    options={[
                        {value: "", label: "Personal / no family member"},
                        ...familyMembers.map((member) => ({value: member.id, label: `${member.name} (${member.role})`})),
                    ]}
                />
            )}

            <Input
                value={income.tags}
                onChange={({target}) => handleChange('tags', target.value)}
                label="Tags"
                placeholder="e.g., salary, side-hustle"
                type="text"
            />

            <Input
                value={income.receiptUrl}
                onChange={({target}) => handleChange('receiptUrl', target.value)}
                label="Receipt URL"
                placeholder="Paste receipt link"
                type="text"
            />

            <textarea
                value={income.notes}
                onChange={({target}) => handleChange('notes', target.value)}
                className="input-box min-h-24"
                placeholder="Notes"
            />

            <div className="flex justify-end mt-6">
                <button
                    onClick={handleAddIncome}
                    disabled={loading}
                    className="add-btn add-btn-fill">
                    {loading ? (
                        <>
                            <LoaderCircle className="w-4 h-4 animate-spin"/>
                            Adding...
                        </>
                    ): (
                        <>
                            Add Income
                        </>
                    )}
                </button>
            </div>
        </div>
    )
}

export default AddIncomeForm;
