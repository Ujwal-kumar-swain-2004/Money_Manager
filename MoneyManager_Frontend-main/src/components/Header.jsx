import {useState} from "react";
import {assets} from "../assets/assets.js";
import {Link} from "react-router-dom";
import {BadgeIndianRupee, Menu, X} from "lucide-react";

const Header = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const navLinks = [
        { name: 'Features', to: '/home#features' },
        { name: 'Friends', to: '/home#friends' },
        { name: 'Family', to: '/home#family' },
        { name: 'Pricing', to: '/home#pricing' }
    ];

    return (
        <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 text-slate-950 backdrop-blur">
            <div className="mx-auto max-w-7xl px-4">
                <div className="flex h-16 items-center justify-between">
                    <Link to="/home" className="flex items-center gap-2">
                        <span className="inline-flex h-10 w-10 items-center justify-center rounded-md bg-slate-950 text-[#d9ff72]">
                            <BadgeIndianRupee size={22} />
                        </span>
                        <span className="text-lg font-bold text-slate-950 truncate">Money Manager</span>
                    </Link>
                    <div className="hidden">
                        <img src={assets.logo} alt="" />
                    </div>

                    <nav className="hidden items-center space-x-8 lg:flex">
                        {navLinks.map((link) => (
                            <a href={link.to} key={link.name} className="text-sm font-semibold text-slate-600 transition-colors hover:text-slate-950">
                                {link.name}
                            </a>
                        ))}
                    </nav>
                    <div className="flex items-center space-x-4">
                        <div className="hidden sm:flex items-center space-x-4">
                            <Link to="/login" className="text-sm font-semibold text-slate-600 transition-colors hover:text-slate-950">
                                Login
                            </Link>
                            <Link
                                to="/signup"
                                className="rounded-md bg-slate-950 px-4 py-2 text-sm font-semibold text-white transition-colors hover:bg-slate-800"
                            >
                                Get Started
                            </Link>
                        </div>
                        <button
                            onClick={() => setIsMenuOpen(!isMenuOpen)}
                            className="rounded-md p-2 text-slate-700 lg:hidden hover:bg-slate-100"
                            aria-label="Toggle menu"
                        >
                            {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile Navigation */}
            {isMenuOpen && (
                <div className="border-t border-slate-200 bg-white lg:hidden">
                    <div className="container mx-auto px-4 py-4">
                        <nav className="flex flex-col space-y-4">
                            {navLinks.map((link) => (
                                <a key={link.name} href={link.to} className="text-slate-700 transition-colors hover:text-slate-950">
                                    {link.name}
                                </a>
                            ))}
                            <div className="flex flex-col space-y-3 pt-4 border-t border-slate-200">
                                <Link to="/login" className="w-full text-left text-slate-700 transition-colors hover:text-slate-950">
                                    Login
                                </Link>
                                <Link
                                    to="/signup"
                                    className="rounded-md bg-slate-950 px-4 py-2 text-center font-semibold text-white transition-colors hover:bg-slate-800"
                                >
                                    Get Started
                                </Link>
                            </div>
                        </nav>
                    </div>
                </div>
            )}
        </header>
    );
};

export default Header;

