import {Trash2, TrendingDown, TrendingUp, UtensilsCrossed} from "lucide-react";
import {addThousandsSeparator} from "../util/util.js";

const TransactionInfoCard = ({icon, title, date, amount, type, hideDeleteBtn, onDelete}) => {
    const getAmountStyles = () => type === 'income'? 'bg-emerald-50 text-emerald-800': 'bg-rose-50 text-rose-800';
    return (
        <div className="group relative flex items-center gap-4 rounded-lg border border-white/14 bg-white/[0.03] p-3 transition hover:border-[#d9ff72]/30 hover:bg-white/[0.06]">
            <div className="flex h-11 w-11 items-center justify-center rounded-md bg-white/[0.05] text-xl text-white">
                {icon ? (
                    <img src={icon} alt={title} className="w-6 h-6" />
                ): (
                    <UtensilsCrossed className="text-[#31572c]" />
                )}
            </div>

            <div className="flex-1 flex items-center justify-between">
                <div>
                    <p className="text-sm font-semibold text-white">{title}</p>
                    <p className="text-xs text-white/72 mt-1">{date}</p>
                </div>

                <div className="flex items-center gap-2">
                    {!hideDeleteBtn && (
                        <button
                            onClick={onDelete}
                            className="icon-btn opacity-0 transition-opacity group-hover:opacity-100">
                            <Trash2 size={18} />
                        </button>
                    )}

                    <div className={`flex items-center gap-2 rounded-md px-3 py-1.5 ${getAmountStyles()}`}>
                        <h6 className="text-xs font-semibold">
                            {type === 'income' ? '+': '-'} ₹{addThousandsSeparator(amount)}
                        </h6>
                        {type === 'income'? (
                            <TrendingUp size={15} />
                        ) : (
                            <TrendingDown size={15}/>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}

export default TransactionInfoCard;

