import { Navigate } from "react-router-dom";

export default function PrivateRoute({
    children,
    role
}) {

    const token =
        localStorage.getItem("token");

    const user =
        JSON.parse(
            localStorage.getItem("user")
        );

    // Chưa đăng nhập
    if (!token || !user) {

        localStorage.removeItem("token");
        localStorage.removeItem("user");

        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }

    // Sai role
    if (
        role &&
        user.role !== role
    ) {

        return (
            <Navigate
                to="/"
                replace
            />
        );
    }

    return children;
}