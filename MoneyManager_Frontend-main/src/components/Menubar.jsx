import { useState, useRef, useEffect, useContext } from "react";
import {User, LogOut, X, Menu} from "lucide-react";
import { useNavigate } from "react-router-dom";
import {assets} from "../assets/assets.js";
import {AppContext} from "../context/AppContext.jsx";
import Sidebar from "./Sidebar.jsx";

const Menubar = ({ activeMenu }) => {
    const [openSideMenu, setOpenSideMenu] = useState(false);
    const [showDropdown, setShowDropdown] = useState(false);
    const dropdownRef = useRef(null);
    const { clearUser, user } = useContext(AppContext);
    const navigate = useNavigate();

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setShowDropdown(false);
            }
        };
        if (showDropdown) {
            document.addEventListener("mousedown", handleClickOutside);
        }
        return () => {
            document.removeEventListener("mousedown", handleClickOutside);
        };
    }, [showDropdown]);

    const toggleDropdown = () => {
        setShowDropdown(!showDropdown);
    };

    const handleLogout = () => {
        localStorage.clear();
        clearUser();
        setShowDropdown(false);
        navigate("/login");
    };

    return (
        <div className="flex items-center justify-between gap-5 bg-[#101914] text-white border-b border-white/10 py-3 px-4 sm:px-7 sticky top-0 z-30">
           
            <div className="flex items-center gap-5">
                <button
                    className="block lg:hidden text-white hover:bg-white/10 p-1 rounded transition-colors"
                    onClick={() => {
                        setOpenSideMenu(!openSideMenu);
                    }}
                >
                    {openSideMenu ? (
                        <X className="text-2xl" />
                    ) : (
                        <Menu className="text-2xl" />
                    )}
                </button>

                <div className="flex items-center gap-2">
                    <img src={assets.logo} alt="logo" className="h-10 w-10" />
                    <span className="text-lg font-semibold text-white truncate">Money Manager</span>
                </div>
            </div>

            <div className="relative" ref={dropdownRef}>
                <button
                    onClick={toggleDropdown}
                    className="flex items-center justify-center w-10 h-10 bg-white/10 hover:bg-white/20
                    rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-purple-800
                    focus:ring-offset-2"
                >
                    <img src={user?.profileImageUrl} alt="profile" className="w-10 h-10 rounded-full"/>
                </button>

                {showDropdown && (
                    <div className="absolute right-0 mt-2 w-48 rounded-lg border border-white/10 bg-[#0d1711] py-1 shadow-lg z-50">
                        
                        <div className="px-4 py-3 border-b border-white/10">
                            <div className="flex items-center gap-3">
                                <div className="flex items-center justify-center w-8 h-8 bg-white/[0.06] rounded-full">
                                    {user? (
                                        <>
                                            <img src={user.profileImageUrl} alt="profile" />
                                        </>
                                    ): (
                                        <>
                                            <User className="w-4 h-4 text-purple-600"/>
                                        </>
                                    )}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-medium text-white truncate">
                                        {user.fullName}
                                    </p>
                                    <p className="text-xs text-white/45 truncate">{user.email}</p>
                                </div>
                            </div>
                        </div>

                        <div className="py-1">
                            <button
                                onClick={handleLogout}
                                className="flex items-center gap-3 w-full px-4 py-2 text-sm text-white/70 hover:bg-white/10 transition-colors duration-150"
                            >
                                <LogOut className="w-4 h-4 text-white/45" />
                                <span>Logout</span>
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {openSideMenu && (
                <div className="fixed top-[73px] left-0 right-0 bg-[#101914] border-b border-white/10 lg:hidden z-20">
                    <Sidebar activeMenu={activeMenu} />
                </div>
            )}
        </div>
    );
};

export default Menubar;
