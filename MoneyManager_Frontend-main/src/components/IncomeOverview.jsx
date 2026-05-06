import {useEffect, useState} from "react";
import {prepareIncomeLineChartData} from "../util/util.js";
import CustomLineChart from "./CustomLineChart.jsx";
import {Plus} from "lucide-react";

const IncomeOverview = ({transactions, onAddIncome}) => {
    const [chartData, setChartData] = useState([]);
    useEffect(() => {
        const result = prepareIncomeLineChartData(transactions);
        console.log(result);
        setChartData(result);

        return () => {};
    }, [transactions]);
    return (
        <div className="panel p-5">
            <div className="flex items-center justify-between">
                <div>
                    <h5 className="section-title">
                        Income Overview
                    </h5>
                    <p className="text-xs text-gray-400 mt-0 5">
                        Track your earnings over time and analyze your income trends.
                    </p>
                </div>
                <button className="add-btn" onClick={onAddIncome}>
                    <Plus size={15} className="text-lg" /> Add Income
                </button>
            </div>
            <div className="mt-6">
                <CustomLineChart data={chartData} />
            </div>
        </div>
    )
}

export default IncomeOverview;

