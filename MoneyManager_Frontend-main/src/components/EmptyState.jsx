import {Inbox} from "lucide-react";

const EmptyState = ({title = "Nothing here yet", description}) => {
    return (
        <div className="rounded-lg border border-dashed border-white/14 bg-white/[0.03] px-5 py-10 text-center">
            <div className="mx-auto mb-3 flex h-11 w-11 items-center justify-center rounded-md bg-white/[0.04] text-white/78 shadow-sm">
                <Inbox size={21} />
            </div>
            <p className="font-medium text-white">{title}</p>
            {description && <p className="mx-auto mt-1 max-w-sm text-sm text-white/65">{description}</p>}
        </div>
    );
};

export default EmptyState;

