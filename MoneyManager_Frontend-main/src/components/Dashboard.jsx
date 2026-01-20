import Menubar from "./Menubar.jsx";
import Sidebar from "./Sidebar.jsx";
import {useContext} from "react";
import {AppContext} from "../context/AppContext.jsx";

const Dashboard = ({children, activeMenu}) => {
    const {user} = useContext(AppContext);
    return (
        <div className="min-h-screen bg-[#070c09]">
            <Menubar activeMenu={activeMenu} />

            {user && (
                <div className="flex">
                    <div className="max-[1080px]:hidden">
                        <Sidebar activeMenu={activeMenu}/>
                    </div>

                    <main className="grow px-4 py-5 md:px-6">{children}</main>
                </div>
            )}
        </div>
    )
}

export default Dashboard;
