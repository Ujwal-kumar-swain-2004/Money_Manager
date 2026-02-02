const InfoCard = ({icon, label, value, color}) => {
    return(
        <div className="panel flex items-center gap-4 p-5">
            <div className={`flex h-12 w-12 items-center justify-center rounded-md text-[24px] text-white ${color}`}>
                {icon}
            </div>
            <div>
                <h6 className="text-xs font-semibold uppercase text-white/45">{label}</h6>
                <span className="mt-1 block text-2xl font-semibold text-white">&#8377;{value}</span>
            </div>
        </div>
    )
}

export default InfoCard;
