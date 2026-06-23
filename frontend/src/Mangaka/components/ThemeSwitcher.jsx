import { useEffect, useState } from "react";

export default function ThemeSwitcher() {

    const [theme, setTheme] = useState(
        localStorage.getItem("theme") || "light"
    );

    useEffect(() => {

        document.documentElement.setAttribute(
            "data-bs-theme",
            theme
        );

        localStorage.setItem(
            "theme",
            theme
        );

    }, [theme]);

    const toggleTheme = () => {

        setTheme(
            prevTheme =>
                prevTheme === "light"
                    ? "dark"
                    : "light"
        );

    };

    return (
        <button
            className="btn btn-outline-secondary"
            onClick={toggleTheme}
        >
            {
                theme === "light"
                    ? "🌙 Dark Mode"
                    : "☀️ Light Mode"
            }
        </button>
    );
}