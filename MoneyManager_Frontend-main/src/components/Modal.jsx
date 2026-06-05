import {useEffect} from "react";
import {X} from "lucide-react";

const Modal = ({ isOpen, onClose, children, title }) => {
    useEffect(() => {
        if (!isOpen) return undefined;
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        return () => {
            document.body.style.overflow = previousOverflow;
        };
    }, [isOpen]);

    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center overflow-hidden bg-black/55 p-3 backdrop-blur-sm md:p-6"
        >
            <div className="flex max-h-[calc(100dvh-1.5rem)] w-full max-w-2xl flex-col overflow-hidden rounded-lg border border-white/14 bg-[#18231d] text-white shadow-2xl md:max-h-[calc(100dvh-3rem)]">
                    <div className="flex shrink-0 items-center justify-between border-b border-white/14 p-5 md:p-6">
                        <h3 className="text-xl font-semibold text-white">
                            {title}
                        </h3>

                        <button
                            type="button"
                            className="icon-btn"
                            onClick={onClose}
                        >
                            <X className="w-4 h-4" />
                        </button>
                    </div>
                    <div className="min-h-0 flex-1 overflow-y-auto overscroll-contain p-5 text-white/75 md:p-6">
                        {children}
                    </div>
            </div>
        </div>
    );
};

export default Modal;

