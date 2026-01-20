const PageHeader = ({eyebrow, title, description, action}) => {
    return (
        <div className="mb-5 flex flex-col gap-4 rounded-lg bg-[#101914] p-5 text-white md:flex-row md:items-end md:justify-between">
            <div>
                {eyebrow && <p className="text-xs font-semibold uppercase text-[#d9ff72]">{eyebrow}</p>}
                <h1 className="mt-1 text-2xl font-semibold md:text-3xl">{title}</h1>
                {description && <p className="mt-2 max-w-2xl text-sm text-white/65">{description}</p>}
            </div>
            {action}
        </div>
    );
};

export default PageHeader;
