import React from "react";
import {X} from "lucide-react";

const Modal = ({ isOpen, onClose, children, title }) => {
    if (!isOpen) return null;

    return (
        <div
            className="fixed inset-0 z-50 flex justify-center items-center w-full h-full overflow-hidden bg-black/40
      backdrop-blur-sm"
        >
            <div className="relative p-4 w-full max-w-2xl max-h-[90vh]">
             
                <div className="relative rounded-lg border border-white/14 bg-[#18231d] text-white shadow-2xl">
                   
                    <div className="flex items-center justify-between border-b border-white/14 p-5 md:p-6">
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
                    <div className="p-5 md:p-6 text-white/75">
                        {children}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Modal;

