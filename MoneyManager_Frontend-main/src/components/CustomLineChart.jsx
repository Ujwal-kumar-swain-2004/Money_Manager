import {
    ResponsiveContainer,
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
} from 'recharts';
import {addThousandsSeparator} from "../util/util.js";

const CustomLineChart = ({ data }) => {
    const CustomTooltip = ({ active, payload, label }) => {
        if (active && payload && payload.length) {
            const dataPoint = payload[0].payload;
            const groupedItemsForTooltip = dataPoint.items.reduce((acc, item) => {
                const { categoryName, amount } = item;
                if (!acc[categoryName]) {
                    acc[categoryName] = {
                        categoryName: categoryName,
                        totalAmount: 0,
                    };
                }
                acc[categoryName].totalAmount += amount;
                return acc;
            }, {});
            const categoriesInTooltip = Object.values(groupedItemsForTooltip);

            return (
                <div className="bg-[#18231d] shadow-md rounded-lg p-2 border border-white/14 text-white">
                 
                    <p className="text-sm font-semibold text-white mb-2">{label}</p>
                    <hr className="my-1 border-white/14" />
                 
                    <p className="text-sm text-white/78 font-bold mb-2">
                        Total: <span className="text-[#d9ff72]">&#8377;{addThousandsSeparator(dataPoint.totalAmount)}</span>
                    </p>


                    {categoriesInTooltip && categoriesInTooltip.length > 0 && (
                        <div className="mt-2 pt-2 border-t border-white/14">
                            <p className="text-xs font-semibold text-white/65 mb-1">Details:</p>
                            {categoriesInTooltip.map((groupedItem, index) => (
                                <div key={index} className="flex justify-between text-xs text-white/65">
                                    <span>{groupedItem.categoryName}:</span>
                                    <span>&#8377;{addThousandsSeparator(groupedItem.totalAmount)}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            );
        }
        return null;
    };

    return (
        <div>
            <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={data}>
                    <defs>
                        <linearGradient id="expenseGradient" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="5%" stopColor="#d9ff72" stopOpacity={0.35} />
                            <stop offset="95%" stopColor="#d9ff72" stopOpacity={0} />
                        </linearGradient>
                    </defs>

                    <CartesianGrid stroke="rgba(255,255,255,.06)" vertical={false} />
                    <XAxis dataKey="month" tick={{ fontSize: 12, fill: "rgba(255,255,255,.45)" }} stroke="none" />
                    <YAxis tick={{ fontSize: 12, fill: "rgba(255,255,255,.45)" }} stroke="none" />
                    <Tooltip content={<CustomTooltip />} />

                    <Area
                        type="monotone"
                        dataKey="totalAmount"
                        stroke="#d9ff72"
                        fill="url(#expenseGradient)"
                        strokeWidth={3}
                        dot={{ r: 3, fill: "#d9ff72" }}
                    />
                </AreaChart>
            </ResponsiveContainer>
        </div>
    );
};

export default CustomLineChart;

