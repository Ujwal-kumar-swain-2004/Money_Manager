import {useState} from "react";
import {Eye, EyeOff} from "lucide-react";

const Input = ({label, value, onChange, placeholder, type, isSelect, options}) => {
    const [showPassword, setShowPassword] = useState(false);

    const toggleShowPassword = () => {
        setShowPassword(!showPassword);
    }
    return (
        <div className="mb-4">
            <label className="text-[13px] font-medium text-white/72 block mb-1">
                {label}
            </label>
            <div className="relative">
                {isSelect ? (
                    <select
                        className="w-full bg-black/15 outline-none border border-white/14 rounded-md py-2 px-3 text-white leading focus:outline-none focus:border-[#d9ff72]/70"
                        value={value}
                        onChange={(e) => onChange(e)}
                    >
                        {options.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                ): (
                    <input
                        className="w-full bg-black/15 outline-none border border-white/14 rounded-md py-2 px-3 pr-10 text-white leading-tight focus:outline-none focus:border-[#d9ff72]/70 placeholder:text-white/55"
                        type={type === 'password' ? (showPassword ? 'text' : 'password') : type}
                        placeholder={placeholder}
                        value={value}
                        onChange={(e) => onChange(e)} />
                )}

                {type === 'password' && (
                    <span className="absolute right-3 top-1/2 -translate-y-1/2 cursor-pointer">
                        {showPassword ? (
                            <Eye
                                size={20}
                                className="text-primary"
                                onClick={toggleShowPassword}
                            />
                        ) : (
                            <EyeOff
                                size={20}
                                className="text-slate-400"
                                onClick={toggleShowPassword}
                            />
                        )}
                    </span>
                )}
            </div>
        </div>
    )
}

export default Input;

