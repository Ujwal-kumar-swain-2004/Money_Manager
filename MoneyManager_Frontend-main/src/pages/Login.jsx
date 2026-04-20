import {useContext, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {assets} from "../assets/assets.js";
import Input from "../components/Input.jsx";
import {validateEmail} from "../util/validation.js";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import {AppContext} from "../context/AppContext.jsx";
import {BadgeIndianRupee, LoaderCircle, ShieldCheck, Sparkles} from "lucide-react";
import Header from "../components/Header.jsx";

const Login = () => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const {setUser} = useContext(AppContext);

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
       
        if (!validateEmail(email)) {
            setError("Please enter valid email address");
            setIsLoading(false);
            return;
        }

        if (!password.trim()) {
            setError("Please enter your password");
            setIsLoading(false);
            return;
        }

        setError("");
        try {
            const response = await axiosConfig.post(API_ENDPOINTS.LOGIN, {
                email,
                password,
            });
            const {token, user} = response.data;
            if (token) {
                localStorage.setItem("token", token);
                setUser(user);
                navigate("/dashboard");
            }
        }catch(error) {
            if (error.response && error.response.data.message) {
                setError(error.response.data.message);
            } else {
                console.error('Something went wrong', error);
                setError(error.message);
            }
        } finally {
            setIsLoading(false);
        }

    }

    return (
        <div className="min-h-screen w-full bg-[#111713]">
            <Header />
            <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 lg:grid-cols-[0.95fr_1.05fr]">
                <div className="hidden rounded-lg bg-[#1f2a24] p-8 text-white lg:block">
                    <p className="text-sm font-semibold uppercase text-[#d9ff72]">Welcome back</p>
                    <h1 className="mt-3 text-4xl font-semibold">Pick up exactly where your money left off.</h1>
                    <div className="mt-10 grid gap-3">
                        <AuthStat icon={BadgeIndianRupee} title="Cashflow" text="Review income, expense, and balance instantly." />
                        <AuthStat icon={ShieldCheck} title="Private workspace" text="Your data stays in your account behind token auth." />
                        <AuthStat icon={Sparkles} title="Smart insights" text="Ask the AI advisor about your real spending." />
                    </div>
                </div>
                <div className="flex items-center justify-center">
                    <div className="panel w-full max-w-md p-8">
                        <h3 className="text-2xl font-semibold text-white text-center mb-2">
                            Welcome Back
                        </h3>
                        <p className="text-sm text-white/65 text-center mb-8">
                            Sign in to manage budgets, transactions, and savings goals.
                        </p>

                        <form onSubmit={handleSubmit} className="space-y-4">

                            <Input
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                label="Email Address"
                                placeholder="name@example.com"
                                type="text"
                            />

                            <Input
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                label="Password"
                                placeholder="*********"
                                type="password"
                            />

                            {error && (
                                <p className="text-red-100 text-sm text-center bg-red-500/15 border border-red-300/20 p-2 rounded">
                                    {error}
                                </p>
                            )}

                            <button disabled={isLoading} className={`btn-primary w-full py-3 text-lg font-medium flex items-center justify-center gap-2 ${isLoading ? 'opacity-60 cursor-not-allowed': ''}`} type="submit">
                                {isLoading ? (
                                    <>
                                        <LoaderCircle className="animate-spin w-5 h-5" />
                                        Logging in...
                                    </>
                                ):("LOGIN")}
                            </button>

                            <p className="text-sm text-white/72 text-center mt-6">
                                Don't have an account?
                                <Link to="/signup" className="ml-1 font-semibold text-[#31572c] underline">Signup</Link>
                            </p>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    )
}

const AuthStat = ({icon: Icon, title, text}) => (
    <div className="rounded-lg border border-white/14 bg-white/10 p-4">
        <Icon className="text-[#d9ff72]" size={20} />
        <p className="mt-3 font-semibold">{title}</p>
        <p className="mt-1 text-sm text-white/72">{text}</p>
    </div>
);

export default Login;

