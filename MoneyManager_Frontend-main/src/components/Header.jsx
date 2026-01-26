import {useState} from "react";
import {assets} from "../assets/assets.js";
import {Link} from "react-router-dom";
import {Menu, X} from "lucide-react";

const Header = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const navLinks = [
        { name: 'Home', to: '/home' },
        { name: 'Dashboard', to: '/dashboard' },
        { name: 'Money Plan', to: '/money-plan' }
    ];

    return (
        <header className="sticky top-0 z-30 border-b border-white/10 bg-[#101914] text-white">
            <div className="mx-auto max-w-7xl px-4">
                <div className="flex h-16 items-center justify-between">
                    {/* Logo */}
                    <div className="flex items-center gap-2">
                        <img src={assets.logo} alt="logo" className="h-10 w-10" />
                        <span className="text-lg font-bold text-white truncate">Money Manager</span>
                    </div>

                    <nav className="hidden items-center space-x-8 lg:flex">
                        {navLinks.map((link) => (
                            <Link to={link.to} key={link.name} className="text-sm font-medium text-white/65 transition-colors hover:text-white">
                                {link.name}
                            </Link>
                        ))}
                    </nav>
                    <div className="flex items-center space-x-4">
                        <div className="hidden sm:flex items-center space-x-4">
                            <Link to="/login" className="text-sm font-medium text-white/70 transition-colors hover:text-white">
                                Login
                            </Link>
                            <Link
                                to="/signup"
                                className="rounded-md bg-[#d9ff72] px-4 py-2 text-sm font-semibold text-[#101914] transition-colors hover:bg-[#c9f35b]"
                            >
                                Get Started
                            </Link>
                        </div>
                        <button
                            onClick={() => setIsMenuOpen(!isMenuOpen)}
                            className="rounded-md p-2 text-white lg:hidden hover:bg-white/10"
                            aria-label="Toggle menu"
                        >
                            {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
                        </button>
                    </div>
                </div>
            </div>

            {/* Mobile Navigation */}
            {isMenuOpen && (
                <div className="border-t border-white/10 bg-[#101914] lg:hidden">
                    <div className="container mx-auto px-4 py-4">
                        <nav className="flex flex-col space-y-4">
                            {navLinks.map((link) => (
                                <Link key={link.name} to={link.to} className="text-white/70 transition-colors hover:text-white">
                                    {link.name}
                                </Link>
                            ))}
                            <div className="flex flex-col space-y-3 pt-4 border-t border-white/10">
                                <Link to="/login" className="w-full text-left text-white/70 transition-colors hover:text-white">
                                    Login
                                </Link>
                                <Link
                                    to="/signup"
                                    className="rounded-md bg-[#d9ff72] px-4 py-2 text-center font-semibold text-[#101914] transition-colors hover:bg-[#c9f35b]"
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
