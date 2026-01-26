import {assets} from "../assets/assets.js";

const ProductShowcase = () => {
    return (
        <section className="bg-[#070c09] py-12">
            <div className="mx-auto max-w-7xl px-4">
                <img
                    src={assets.landing}
                    className="h-auto w-full rounded-lg border border-white/10 object-cover shadow-sm"
                    alt="MoneyWise App Dashboard"
                    onError={(e) => { e.target.onerror = null; e.target.src='https://placehold.co/1200x600/E2E8F0/4A5568?text=Image+Not+Found'; }}
                />
            </div>
        </section>
    );
};

export default ProductShowcase;
