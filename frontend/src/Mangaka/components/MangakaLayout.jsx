import { Outlet, NavLink, useNavigate } from "react-router-dom";
import "../styles/MangakaLayout.css";
import ThemeProvider from "../../components/ThemeProvider";

export default function MangakaLayout() {

    const navigate = useNavigate();

    let user = null;

    try {
        user = JSON.parse(
            localStorage.getItem("user")
        );
    } catch (error) {
        console.error("User parse error:", error);
        user = null;
    }

    const handleLogout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("user");

        navigate("/login");
    };

    return (
        <div className="container-fluid">
            <ThemeProvider />
            <div className="row">

                {/* Sidebar */}

                <div className="col-md-2 sidebar">

                    <div className="sidebar-header">

                        <h3 className="sidebar-title">
                            Manga Manager
                        </h3>

                        <small>
                            Mangaka Portal
                        </small>

                    </div>

                    <ul className="nav flex-column mt-4">

                        <li>
                            <NavLink
                                to="/mangaka"
                                end
                                className={({ isActive }) =>
                                    `sidebar-link ${isActive ? "active-link" : ""}`
                                }
                            >
                                📊 Dashboard
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/manga"
                                className={({ isActive }) =>
                                    `sidebar-link ${isActive ? "active-link" : ""}`
                                }
                            >
                                📚 My Series
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/tasks"
                                className={({ isActive }) =>
                                    `sidebar-link ${isActive ? "active-link" : ""}`
                                }
                            >
                                🎨 Assistant Tasks
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/ranking"
                                className={({ isActive }) =>
                                    `sidebar-link ${isActive ? "active-link" : ""}`
                                }
                            >
                                🏆 Ranking
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/notifications"
                                className={({ isActive }) =>
                                    `sidebar-link ${isActive ? "active-link" : ""}`
                                }
                            >
                                🔔 Notifications
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/settings"
                                className={({ isActive }) =>
                                    `sidebar-link ${isActive ? "active-link" : ""}`
                                }
                            >
                                ⚙️ Settings
                            </NavLink>
                        </li>

                        <li className="mt-4">
                            <button
                                className="logout-btn"
                                onClick={handleLogout}
                            >
                                🚪 Logout
                            </button>
                        </li>

                    </ul>

                </div>

                {/* Content */}

                <div className="col-md-10 p-4">

                    <div className="card shadow-sm border-0 mb-4">

                        <div className="card-body d-flex justify-content-between align-items-center">

                            <div>

                                <h4 className="mb-0">
                                    {
                                        user
                                            ? `Xin chào, ${user.username}`
                                            : "Chưa có thông tin tài khoản"
                                    }
                                </h4>

                                <small className="text-muted">
                                    {
                                        user?.role || "Guest"
                                    }
                                </small>

                            </div>

                            <div className="text-end">

                                <span className="badge bg-success">
                                    Online
                                </span>

                                <br />

                                <small className="text-muted">

                                    {user?.role ||
                                        "ROLE_UNKNOWN"}

                                </small>

                            </div>

                        </div>

                    </div>

                    <Outlet />

                </div>

            </div>

        </div>
    );
}