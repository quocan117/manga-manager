import { Outlet, NavLink, useNavigate } from "react-router-dom";
import "../styles/MangakaLayout.css";

export default function MangakaLayout() {
    const navigate = useNavigate();

    return (
        <div className="container-fluid">
            <div className="row">

                <div className="col-md-2 sidebar">

                    <h3 className="sidebar-title">
                        Mangaka
                    </h3>

                    <ul className="nav flex-column mt-4">

                        <li>
                            <NavLink
                                to="/mangaka"
                                end
                                className={({ isActive }) =>
                                    `sidebar-link ${
                                        isActive ? "active-link" : ""
                                    }`
                                }
                            >
                                📊 Dashboard
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/manga"
                                className={({ isActive }) =>
                                    `sidebar-link ${
                                        isActive ? "active-link" : ""
                                    }`
                                }
                            >
                                📚 My Manga
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/notifications"
                                className={({ isActive }) =>
                                    `sidebar-link ${
                                        isActive ? "active-link" : ""
                                    }`
                                }
                            >
                                🔔 Notifications
                            </NavLink>
                        </li>

                        <li>
                            <NavLink
                                to="/mangaka/settings"
                                className={({ isActive }) =>
                                    `sidebar-link ${
                                        isActive ? "active-link" : ""
                                    }`
                                }
                            >
                                ⚙️ Settings
                            </NavLink>
                        </li>

                        <li className="mt-4">
                            <button
                                className="logout-btn"
                                onClick={() => navigate("/")}
                            >
                                🚪 Logout
                            </button>
                        </li>

                    </ul>
                </div>

                <div className="col-md-10 p-4">

                    <div className="mb-4">
                        <h4>
                            Xin chào, Eiichiro Oda
                        </h4>
                    </div>

                    <Outlet />

                </div>

            </div>
        </div>
    );
}