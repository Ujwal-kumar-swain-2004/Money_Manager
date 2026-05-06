import {addThousandsSeparator} from "../util/util.js";


const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
        return (
            <div className="bg-[#18231d] shadow-md rounded-lg p-2 border border-white/14">
                <p className="text-xs font-semibold text-[#d9ff72] mb-1">{payload[0].name}</p>
                <p className="text-sm text-white/72">
                    Amount: <span className="text-sm font-medium text-white">&#8377;{addThousandsSeparator(payload[0].value)}</span>
                </p>
            </div>
        );
    }
    return null;
};

export default CustomTooltip;

