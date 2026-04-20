import Header from "../components/Header.jsx";
import HeroSection from "../components/HeroSection.jsx";
import ProductShowcase from "../components/ProductShowcase.jsx";

const LandingPage = () => {
    return (
        <div className="bg-[#111713] font-sans text-white">
            <Header />
            <main>
                <HeroSection/>
                <ProductShowcase />
            </main>
        </div>
    )
}

export default LandingPage;

