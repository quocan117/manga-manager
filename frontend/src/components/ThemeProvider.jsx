import { useEffect } from "react";

export default function ThemeProvider() {

    useEffect(() => {

        const savedTheme =
            localStorage.getItem("theme") || "light";

        document.documentElement.setAttribute(
            "data-bs-theme",
            savedTheme
        );

        return () => {

            document.documentElement.setAttribute(
                "data-bs-theme",
                "light"
            );

        };

    }, []);

    return null;
}