import Header from "../components/Header.jsx";
import HeroSection from "../components/HeroSection.jsx";
import ProductShowcase from "../components/ProductShowcase.jsx";

const LandingPage = () => {
    return (
        <div className="bg-white font-sans text-slate-950">
            <Header />
            <main>
                <HeroSection/>
                <ProductShowcase />
            </main>
        </div>
    )
}

export default LandingPage;

