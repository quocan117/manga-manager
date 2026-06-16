import { useState } from "react";
import { trendingSeries } from "../../data/mockData";
import { useNavigate } from "react-router-dom";

export default function MyManga() {
    const [mangas, setMangas] = useState(trendingSeries);
    const navigate = useNavigate();
    return (
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
    );
}