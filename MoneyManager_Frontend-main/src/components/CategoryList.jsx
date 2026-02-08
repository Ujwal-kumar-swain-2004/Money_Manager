import {Layers2, Pencil} from "lucide-react";
import EmptyState from "./EmptyState.jsx";

const CategoryList = ({categories, onEditCategory}) => {
    return (
        <div className="panel p-5">
            <div className="flex items-center justify-between mb-4">
                <h4 className="section-title">Category Library</h4>
            </div>

            {categories.length === 0 ? (
                <EmptyState title="No categories yet" description="Create income and expense categories to organize every transaction." />
            ): (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
                    {categories.map((category) => (
                        <div
                            key={category.id}
                            className="group relative flex items-center gap-4 rounded-lg border border-white/10 bg-white/[0.03] p-4 transition hover:border-[#d9ff72]/30 hover:bg-white/[0.06]">
                          
                            <div className="flex h-12 w-12 items-center justify-center rounded-md bg-white/[0.05] text-xl text-white">
                                {category.icon ? (
                                    <span className="text-2xl">
                                        <img src={category.icon} alt={category.name} className="h-5 w-5" />
                                    </span>
                                ): (
                                    <Layers2 className="text-[#31572c]" size={24} />
                                )}
                            </div>


                            <div className="flex-1 flex items-center justify-between">
                              
                                <div>
                                    <p className="text-sm text-white font-semibold">
                                        {category.name}
                                    </p>
                                    <p className="mt-1 inline-flex rounded-md bg-white/[0.06] px-2 py-0.5 text-xs font-medium capitalize text-white/55">
                                        {category.type}
                                    </p>
                                </div>
                               
                                <div className="flex items-center gap-2">
                                    <button
                                        onClick={() => onEditCategory(category)}
                                        className="icon-btn opacity-0 transition-opacity group-hover:opacity-100">
                                        <Pencil size={18} />
                                    </button>
                                </div>
                            </div>

                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default CategoryList;
