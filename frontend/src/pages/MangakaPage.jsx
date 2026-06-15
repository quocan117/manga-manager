import { useState } from "react";
import { trendingSeries } from "../data/mockData";
import { useNavigate } from "react-router-dom";

export default function MangakaPage() {
    const [mangas, setMangas] = useState(trendingSeries);
    const navigate = useNavigate();
    //**Chạy và gọi API thì dổi sang cái này, sửa API ở bên authService.js */
    // useEffect(() => {
    //     fetchMangas();
    // }, []);

    // const fetchMangas = async () => {
    //     const data = await mangaService.getMyMangas();

    //     setMangas(data);
    // };

    const [notifications] = useState([
        "Chapter 20 đã được duyệt",
        "Manga Dark Hunter đang được review",
        "Có feedback mới từ Editor",
    ]);

    return (
        <div className="container-fluid">
            <div className="row">

                {/* Sidebar */}
                <div className="col-md-2 bg-dark text-white min-vh-100 p-3">
                    <h3>Mangaka</h3>

                    <ul className="nav flex-column mt-4">
                        <li className="nav-item mb-2">
                            Dashboard
                        </li>

                        <li className="nav-item mb-2">
                            My Manga
                        </li>

                        <li className="nav-item mb-2">
                            Notifications
                        </li>

                        <li className="nav-item mb-2">
                            Settings
                        </li>

                        <li className="nav-item">
                            Logout
                        </li>
                    </ul>
                </div>

                {/* Main Content */}
                <div className="col-md-10 p-4">

                    <h2>Xin chào, Thành Manga</h2>

                    {/* Statistics */}
                    <div className="row mt-4">

                        <div className="col-md-4">
                            <div className="card shadow">
                                <div className="card-body text-center">
                                    <h5>Total Manga</h5>
                                    <h2>{mangas.length}</h2>
                                </div>
                            </div>
                        </div>

                        <div className="col-md-4">
                            <div className="card shadow">
                                <div className="card-body text-center">
                                    <h5>Published</h5>
                                    <h2>
                                        {
                                            mangas.filter(
                                                m =>
                                                    m.status ===
                                                    "Published"
                                            ).length
                                        }
                                    </h2>
                                </div>
                            </div>
                        </div>

                        <div className="col-md-4">
                            <div className="card shadow">
                                <div className="card-body text-center">
                                    <h5>Draft</h5>
                                    <h2>
                                        {
                                            mangas.filter(
                                                m =>
                                                    m.status ===
                                                    "Draft"
                                            ).length
                                        }
                                    </h2>
                                </div>
                            </div>
                        </div>

                    </div>

                    {/* Manga List */}
                    <div className="card shadow mt-4">
                        <div className="card-header">
                            My Manga
                        </div>

                        <div className="card-body">

                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Title</th>
                                        <th>Status</th>
                                        <th>Chapters</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>

                                <tbody>

                                    {mangas.map((manga) => (
                                        <tr key={manga.id}>
                                            <td>{manga.id}</td>

                                            <td>
                                                {manga.title}
                                            </td>

                                            <td>
                                                {manga.status}
                                            </td>

                                            <td>
                                                {manga.chapters.length}
                                            </td>
                                            <td>
                                                <button
                                                    className="btn btn-primary btn-sm"
                                                    onClick={() => navigate(`/manga/${manga.id}`)}
                                                >
                                                    View
                                                </button>
                                            </td>
                                        </tr>
                                    ))}

                                </tbody>
                            </table>

                        </div>
                    </div>

                    {/* Notifications */}
                    <div className="card shadow mt-4">
                        <div className="card-header">
                            Notifications
                        </div>

                        <div className="card-body">

                            <ul>
                                {notifications.map(
                                    (notification, index) => (
                                        <li key={index}>
                                            {notification}
                                        </li>
                                    )
                                )}
                            </ul>

                        </div>
                    </div>

                </div>

            </div>
        </div>
    );
}