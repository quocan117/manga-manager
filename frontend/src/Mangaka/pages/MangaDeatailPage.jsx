import { useNavigate, useParams } from "react-router-dom";
import { trendingSeries } from "../../data/mockData";

export default function MangaDetailPage() {

    const { id } = useParams();
    const navigate = useNavigate();

    const manga = trendingSeries.find(
        m => m.id === Number(id)
    );

    if (!manga) {
        return <h2>Manga không tồn tại</h2>;
    }

    return (
        <div className="container mt-4">

            <div className="d-flex justify-content-between align-items-center mb-4">

                <h2>{manga.title}</h2>

                <button
                    className="btn btn-success"
                    onClick={() => navigate(`/manga/${manga.id}/create-chapter`)}>
                    ➕ Create Chapter
                </button>

            </div>

            <div className="card shadow mb-4">
                <div className="card-body">

                    <h5>Author</h5>
                    <p>{manga.author}</p>

                    <h5>Description</h5>
                    <p>{manga.description}</p>

                    <h5>Status</h5>
                    <p>{manga.status}</p>

                </div>
            </div>

            <div className="card shadow">

                <div className="card-header">
                    Chapters
                </div>

                <div className="card-body">

                    <table className="table">

                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Votes</th>
                            </tr>
                        </thead>

                        <tbody>

                            {manga.chapters.map(chapter => (
                                <tr key={chapter.id}>
                                    <td>{chapter.id}</td>
                                    <td>{chapter.title}</td>
                                    <td>{chapter.votes}</td>
                                </tr>
                            ))}

                        </tbody>

                    </table>

                </div>

            </div>

        </div>
    );
}