import Dashboard from "../components/Dashboard.jsx";
import {useUser} from "../hooks/useUser.jsx";
import {Plus} from "lucide-react";
import CategoryList from "../components/CategoryList.jsx";
import {useState} from "react";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";
import Modal from "../components/Modal.jsx";
import AddCategoryForm from "../components/AddCategoryForm.jsx";
import PageHeader from "../components/PageHeader.jsx";
import {useQuery, useQueryClient} from "@tanstack/react-query";
import {cacheTimes, queryKeys} from "../util/queryClient.js";

const Category = () => {
    useUser();
    const queryClient = useQueryClient();
    const [openAddCategoryModal, setOpenAddCategoryModal] = useState(false);
    const [openEditCategoryModal, setOpenEditCategoryModal] = useState(false);
    const [selectedCategory, setSelectedCategory] = useState(null);

    const {data: categoryData = [], isLoading: loading} = useQuery({
        queryKey: queryKeys.categories,
        queryFn: async () => {
            const response = await axiosConfig.get(API_ENDPOINTS.GET_ALL_CATEGORIES);
            return response.data || [];
        },
        staleTime: cacheTimes.categories,
        gcTime: cacheTimes.categories * 2,
        onError: (error) => {
            console.error('Full error:', error);
            if (error.response?.status === 401) {
                toast.error("Authentication failed. Please login again.");
                localStorage.removeItem('token');
                window.location.href = '/login';
            } else {
                toast.error(error.response?.data?.message || error.message || 'Failed to fetch categories');
            }
        },
    });

    const handleAddCategory = async (category) => {
        const {name, type, icon} = category;

        if (!name.trim()) {
            toast.error("Category Name is required");
            return;
        }
        const isDuplicate = categoryData.some((category) => {
            return category.name.toLowerCase() === name.trim().toLowerCase();
        })

        if (isDuplicate) {
            toast.error("Category Name already exists");
            return;
        }

        try {
            const response = await axiosConfig.post(API_ENDPOINTS.ADD_CATEGORY, {name, type, icon});
            if (response.status === 201) {
                toast.success("Category added successfully");
                setOpenAddCategoryModal(false);
                queryClient.invalidateQueries({queryKey: queryKeys.categories});
            }
        } catch (error) {
            console.error('Error adding category:', error);
            if (error.response?.status === 401) {
                toast.error("Authentication failed. Please login again.");
                localStorage.removeItem('token');
                window.location.href = '/login';
            } else {
                toast.error(error.response?.data?.message || "Failed to add category.");
            }
        }
    }

    const handleEditCategory = (categoryToEdit) => {
        setSelectedCategory(categoryToEdit);
        setOpenEditCategoryModal(true);
    }

    const handleUpdateCategory = async (updatedCategory) => {
        const {id, name, type, icon} = updatedCategory;
        if (!name.trim()) {
            toast.error("Category Name is required");
            return;
        }

        if (!id) {
            toast.error("Category ID is missing for update");
            return;
        }

        try {
            await axiosConfig.put(API_ENDPOINTS.UPDATE_CATEGORY(id), {name, type, icon});
            setOpenEditCategoryModal(false);
            setSelectedCategory(null);
            toast.success("Category updated successfully");
            queryClient.invalidateQueries({queryKey: queryKeys.categories});
        } catch(error) {
            console.error('Error updating category:', error.response?.data?.message || error.message);
            if (error.response?.status === 401) {
                toast.error("Authentication failed. Please login again.");
                localStorage.removeItem('token');
                window.location.href = '/login';
            } else {
                toast.error(error.response?.data?.message || "Failed to update category.");
            }
        }
    }

    return (
        <Dashboard activeMenu="Category">
            <div className="mx-auto max-w-7xl">
                <PageHeader
                    eyebrow="Organize"
                    title="Categories"
                    description="Create a clean structure for income and expenses so reports, budgets, and AI insights stay useful."
                    action={(
                        <button onClick={() => setOpenAddCategoryModal(true)} className="add-btn">
                            <Plus size={15} />
                            Add Category
                        </button>
                    )}
                />

                {loading ? (
                    <div className="panel p-6 text-center text-sm text-white/70">Loading categories...</div>
                ) : (
                    <CategoryList categories={categoryData} onEditCategory={handleEditCategory} />
                )}

                <Modal
                    isOpen={openAddCategoryModal}
                    onClose={() => setOpenAddCategoryModal(false)}
                    title="Add Category"
                >
                    <AddCategoryForm onAddCategory={handleAddCategory}/>
                </Modal>
                
                <Modal
                    onClose={() =>{
                        setOpenEditCategoryModal(false);
                        setSelectedCategory(null);
                    }}
                    isOpen={openEditCategoryModal}
                    title="Update Category"
                >
                    <AddCategoryForm
                        initialCategoryData={selectedCategory}
                        onAddCategory={handleUpdateCategory}
                        isEditing={true}
                    />
                </Modal>
            </div>
        </Dashboard>
    )
}

export default Category;

