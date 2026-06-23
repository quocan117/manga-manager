import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    getMySeries,
    getSeriesChapters
} from "../../services/mangakaService";

export default function MyManga() {

    const [series, setSeries] = useState([]);
    const [chapters, setChapters] = useState({});
    const [expandedSeries, setExpandedSeries] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const navigate = useNavigate();

    useEffect(() => {
        fetchSeries();
    }, []);

    const fetchSeries = async () => {

        try {

            setLoading(true);
            setError("");

            const data = await getMySeries();

            setSeries(data || []);

        } catch (err) {

            console.error(err);

            setError(
                "Không thể tải danh sách manga từ server."
            );

        } finally {

            setLoading(false);

        }
    };

    const handleShowChapters =
        async (seriesId) => {

            if (expandedSeries === seriesId) {

                setExpandedSeries(null);

                return;
            }

            try {

                if (!chapters[seriesId]) {

                    const data =
                        await getSeriesChapters(
                            seriesId
                        );

                    setChapters(prev => ({
                        ...prev,
                        [seriesId]: data || []
                    }));

                }

                setExpandedSeries(seriesId);

            } catch (err) {

                console.error(err);

                alert(
                    "Không thể tải chapter."
                );
            }
        };

    if (loading) {
        return (
            <div className="text-center mt-5">
                <div className="spinner-border" />
                <p className="mt-3">
                    Đang tải dữ liệu...
                </p>
            </div>
        );
    }

    return (
        <div>

            {/* Header */}

            <div className="d-flex justify-content-between align-items-center mb-4">

                <h2>
                    My Series
                </h2>

                <button
                    className="btn btn-success"
                    onClick={() =>
                        navigate(
                            "/mangaka/create-series"
                        )
                    }
                >
                    + Create Series
                </button>

            </div>

            {/* Error */}

            {error && (

                <div className="alert alert-danger">

                    <strong>Lỗi:</strong>

                    {" "}

                    {error}

                    <div className="mt-2">

                        <button
                            className="btn btn-outline-danger btn-sm"
                            onClick={fetchSeries}
                        >
                            Thử lại
                        </button>

                    </div>

                </div>

            )}

            {/* Empty State */}

            {!loading &&
                !error &&
                series.length === 0 && (

                    <div className="card shadow">

                        <div className="card-body text-center p-5">

                            <h4>
                                Bạn chưa có manga nào
                            </h4>

                            <p className="text-muted">

                                Hãy tạo series đầu tiên
                                để bắt đầu đăng truyện.

                            </p>

                            <button
                                className="btn btn-primary"
                                onClick={() =>
                                    navigate(
                                        "/mangaka/create-series"
                                    )
                                }
                            >
                                + Create Series
                            </button>

                        </div>

                    </div>

                )}

            {/* Series List */}

            {series.map(item => (

                <div
                    key={item.id}
                    className="card shadow mb-4"
                >

                    <div className="card-body">

                        <div className="row">

                            <div className="col-md-2">

                                <img
                                    src={
                                        item.coverUrl
                                            ? `http://localhost:8080/covers/${item.coverUrl}`
                                            : "https://placehold.co/250x350?text=No+Cover"
                                    }
                                    alt={item.title}
                                    className="img-fluid rounded"
                                    onError={(e) => {
                                        e.target.src =
                                            "https://placehold.co/250x350?text=No+Cover";
                                    }}
                                />

                            </div>

                            <div className="col-md-10">

                                <h4>
                                    {item.title}
                                </h4>

                                <p>
                                    {
                                        item.description ||
                                        "Chưa có mô tả"
                                    }
                                </p>

                                <p>

                                    <strong>
                                        Genres:
                                    </strong>

                                    {" "}

                                    {
                                        item.genres?.join(
                                            ", "
                                        ) || "N/A"
                                    }

                                </p>

                                <p>

                                    <strong>
                                        Status:
                                    </strong>

                                    {" "}

                                    <span
                                        className="badge bg-primary"
                                    >
                                        {item.status}
                                    </span>

                                </p>

                                <p>

                                    <strong>
                                        Ranking Score:
                                    </strong>

                                    {" "}

                                    {
                                        item.rankingScore ??
                                        0
                                    }

                                </p>

                                <div className="d-flex gap-2">

                                    <button
                                        className="btn btn-primary"
                                        onClick={() =>
                                            handleShowChapters(
                                                item.id
                                            )
                                        }
                                    >
                                        {
                                            expandedSeries ===
                                                item.id
                                                ? "Hide Chapters"
                                                : "Show Chapters"
                                        }
                                    </button>

                                    <button
                                        className="btn btn-success"
                                        onClick={() =>
                                            navigate(
                                                `/manga/${item.id}/create-chapter`
                                            )
                                        }
                                    >
                                        Create Chapter
                                    </button>

                                </div>

                            </div>

                        </div>

                        {/* Chapters */}

                        {expandedSeries ===
                            item.id && (

                                <div className="mt-4">

                                    <h5>
                                        Chapters
                                    </h5>

                                    {chapters[item.id]
                                        ?.length ===
                                        0 ? (

                                        <div className="alert alert-warning">

                                            Chưa có chapter nào.

                                        </div>

                                    ) : (

                                        <table className="table table-hover">

                                            <thead>

                                                <tr>

                                                    <th>
                                                        Number
                                                    </th>

                                                    <th>
                                                        Title
                                                    </th>

                                                    <th>
                                                        Status
                                                    </th>

                                                    <th>
                                                        Created
                                                    </th>

                                                </tr>

                                            </thead>

                                            <tbody>

                                                {chapters[
                                                    item.id
                                                ]?.map(
                                                    chapter => (

                                                        <tr
                                                            key={
                                                                chapter.id
                                                            }
                                                        >

                                                            <td>
                                                                {
                                                                    chapter.chapterNumber
                                                                }
                                                            </td>

                                                            <td>
                                                                {
                                                                    chapter.title
                                                                }
                                                            </td>

                                                            <td>
                                                                {
                                                                    chapter.status
                                                                }
                                                            </td>

                                                            <td>
                                                                {
                                                                    chapter.createdAt
                                                                        ? new Date(
                                                                            chapter.createdAt
                                                                        ).toLocaleDateString()
                                                                        : "-"
                                                                }
                                                            </td>

                                                        </tr>

                                                    )
                                                )}

                                            </tbody>

                                        </table>

                                    )}

                                </div>

                            )}

                    </div>

                </div>

            ))}

        </div>
    );
}