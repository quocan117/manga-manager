import { Outlet, useNavigate } from "react-router-dom";

export default function MangakaLayout() {
    const navigate = useNavigate();

    return (
        <div className="container-fluid">
            <div className="row">
                {/* Sidebar */}
                <div className="col-md-2 bg-dark text-white min-vh-100 p-3">

                    <h3>Mangaka</h3>

                    <ul className="nav flex-column mt-4">

                        <li
                            className="nav-item mb-2"
                            onClick={() => navigate("/mangaka")}
                        >
                            Dashboard
                        </li>

                        <li
                            className="nav-item mb-2"
                            onClick={() => navigate("/mangaka/manga")}
                        >
                            My Manga
                        </li>

                        <li
                            className="nav-item mb-2"
                            onClick={() => navigate("/mangaka/notifications")}
                        >
                            Notifications
                        </li>

                        <li
                            className="nav-item mb-2"
                            onClick={() => navigate("/mangaka/settings")}
                        >
                            Settings
                        </li>

                        <li
                            className="nav-item text-danger"
                            onClick={() => navigate("/")}
                        >
                            Logout
                        </li>
                    </ul>
                </div>

                {/* Content */}
                <div className="col-md-10 p-4">

                    <div className="mb-4">
                        <h4>Xin chào, Eiichiro Oda</h4>
                    </div>
                    <Outlet />
                </div>
            </div>
        </div>
    );
}