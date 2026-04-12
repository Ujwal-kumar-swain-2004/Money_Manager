import Menubar from "./Menubar.jsx";
import Sidebar from "./Sidebar.jsx";
import {useContext} from "react";
import {AppContext} from "../context/AppContext.jsx";

const Dashboard = ({children, activeMenu}) => {
    const {user} = useContext(AppContext);
    return (
        <div className="min-h-screen bg-[#111713] bg-[radial-gradient(circle_at_top_left,rgba(217,255,114,0.08),transparent_32%),linear-gradient(180deg,#111713_0%,#152019_100%)]">
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

