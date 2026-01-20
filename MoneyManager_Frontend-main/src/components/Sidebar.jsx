import {useContext} from "react";
import {AppContext} from "../context/AppContext.jsx";
import {User} from "lucide-react";
import {SIDE_BAR_DATA} from "../assets/assets.js";
import {useNavigate} from "react-router-dom";

const Sidebar = ({activeMenu}) => {
    const {user} = useContext(AppContext);
    const navigate = useNavigate();
    return (
        <div className="w-64 h-[calc(100vh-61px)] bg-[#101914] text-white border-r border-white/10 p-4 sticky top-[61px] z-20">
            <div className="flex flex-col items-center justify-center gap-3 mt-3 mb-7">
                {user?.profileImageUrl ? (
                    <img src={user?.profileImageUrl || ""} alt="profile image" className="w-20 h-20 bg-slate-400 rounded-full" />
                ): (
                    <User className="w-20 h-20 text-xl" />
                )}
                <h5 className="text-white font-medium leading-6">{user.fullName || ""}</h5>
            </div>
            {SIDE_BAR_DATA.map((item, index) => (
                <button
                    onClick={() => navigate(item.path)}
                    key={`menu_${index}`}
                    className={`cursor-pointer w-full flex items-center gap-3 text-[14px] py-3 px-4 rounded-md mb-2 transition ${activeMenu == item.label ? "text-[#101914] bg-[#d9ff72]": "text-white/70 hover:text-white hover:bg-white/10"}`}>
                      <item.icon className="text-xl" />
                        {item.label}
                </button>
            ))}
        </div>
    )
}

export default Sidebar;
