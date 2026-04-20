import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import {assets} from "../assets/assets.js";
import Input from "../components/Input.jsx";
import {validateEmail} from "../util/validation.js";
import axiosConfig from "../util/axiosConfig.jsx";
import {API_ENDPOINTS} from "../util/apiEndpoints.js";
import toast from "react-hot-toast";
import {BadgeIndianRupee, LoaderCircle, PiggyBank, Repeat2} from "lucide-react";
import ProfilePhotoSelector from "../components/ProfilePhotoSelector.jsx";
import uploadProfileImage from "../util/uploadProfileImage.js";
import Header from "../components/Header.jsx";

const Signup = () => {
    const [fullName, setFullName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const [profilePhoto, setProfilePhoto] = useState(null);

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        let profileImageUrl = "";
        setIsLoading(true);
        if (!fullName.trim()) {
            setError("Please enter your fullname");
            setIsLoading(false);
            return;
        }

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

            if (profilePhoto) {
                const imageUrl = await uploadProfileImage(profilePhoto);
                profileImageUrl = imageUrl || "";
            }
            const response = await axiosConfig.post(API_ENDPOINTS.REGISTER, {
                fullName,
                email,
                password,
                profileImageUrl
            })
            if (response.status === 201) {
                toast.success("Profile created successfully.");
                navigate("/login");
            }
        } catch(err) {
            console.error('Something went wrong', err);
            setError(err.response?.data?.message || err.response?.data?.error || err.message);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="min-h-screen w-full bg-[#111713]">
            <Header />
            <div className="mx-auto grid max-w-7xl gap-8 px-4 py-10 lg:grid-cols-[0.95fr_1.05fr]">
                <div className="hidden rounded-lg bg-[#1f2a24] p-8 text-white lg:block">
                    <p className="text-sm font-semibold uppercase text-[#d9ff72]">Create your workspace</p>
                    <h1 className="mt-3 text-4xl font-semibold">Build better money habits from day one.</h1>
                    <div className="mt-10 grid gap-3">
                        <AuthStat icon={BadgeIndianRupee} title="Track everything" text="Income, expense, tags, notes, receipts, and methods." />
                        <AuthStat icon={PiggyBank} title="Plan ahead" text="Budgets and savings goals built into your flow." />
                        <AuthStat icon={Repeat2} title="Automate memory" text="Recurring income and expenses stay visible." />
                    </div>
                </div>
                <div className="flex items-center justify-center">
                    <div className="panel w-full max-w-lg p-8">
                        <h3 className="text-2xl font-semibold text-white text-center mb-2">
                            Create An Account
                        </h3>
                        <p className="text-sm text-white/65 text-center mb-8">
                            Start with clean categories, budgets, goals, and insights.
                        </p>

                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div className="flex justify-center mb-6">
                                <ProfilePhotoSelector image={profilePhoto} setImage={setProfilePhoto} />
                            </div>
                            <div className="grid grid-cols-2 md:grid-cols-2 gap-4">
                                <Input
                                    value={fullName}
                                    onChange={(e) => setFullName(e.target.value)}
                                    label="Full Name"
                                    placeholder="Jhon Doe"
                                    type="text"
                                />

                                <Input
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    label="Email Address"
                                    placeholder="name@example.com"
                                    type="text"
                                />

                                <div className="col-span-2">
                                    <Input
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        label="Password"
                                        placeholder="*********"
                                        type="password"
                                    />
                                </div>

                            </div>
                            {error && (
                                <p className="text-red-100 text-sm text-center bg-red-500/15 border border-red-300/20 p-2 rounded">
                                    {error}
                                </p>
                            )}

                            <button disabled={isLoading} className={`btn-primary w-full py-3 text-lg font-medium flex items-center justify-center gap-2 ${isLoading ? 'opacity-60 cursor-not-allowed': ''}`} type="submit">
                                {isLoading ? (
                                    <>
                                        <LoaderCircle className="animate-spin w-5 h-5" />
                                        Signing Up...
                                    </>
                                ): (
                                    "SIGN UP"
                                )}
                            </button>

                            <p className="text-sm text-white/72 text-center mt-6">
                                Already have an account?
                                <Link to="/login" className="ml-1 font-semibold text-[#31572c] underline">Login</Link>
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

export default Signup;

