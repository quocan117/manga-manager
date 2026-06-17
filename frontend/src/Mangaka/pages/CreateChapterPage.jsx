import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

export default function CreateChapterPage() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [chapter, setChapter] = useState({
        title: "",
        description: "",
        pdfFile: null,
    });

    const handleChange = (e) => {
        setChapter({
            ...chapter,
            [e.target.name]: e.target.value,
        });
    };

    const handleFileChange = (e) => {
        setChapter({
            ...chapter,
            pdfFile: e.target.files[0],
        });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        console.log({
            mangaId: id,
            title: chapter.title,
            description: chapter.description,
            pdfFile: chapter.pdfFile,
        });

        alert("Chapter đã được gửi để xét duyệt!");

        navigate(`/manga/${id}`);
    };

    return (
        <div className="container mt-4">
            <div
                className="card shadow mx-auto"
                style={{ maxWidth: "900px" }}>
                <div className="card-body p-4">

                    <h2 className="mb-4 text-center">
                        Create Chapter
                    </h2>

                    <form onSubmit={handleSubmit}>

                        <div className="mb-3">
                            <label className="form-label">
                                Chapter Title
                            </label>

                            <input
                                type="text"
                                name="title"
                                className="form-control"
                                value={chapter.title}
                                onChange={handleChange}
                                placeholder="Ví dụ: Chapter 1112 - Trận Chiến Cuối Cùng"
                                required
                            />
                        </div>

                        <div className="mb-3">
                            <label className="form-label">
                                Description
                            </label>

                            <textarea
                                rows="5"
                                name="description"
                                className="form-control"
                                value={chapter.description}
                                onChange={handleChange}
                                placeholder="Mô tả ngắn nội dung chapter"
                            />
                        </div>

                        <div className="mb-4">
                            <label className="form-label">
                                Upload PDF
                            </label>

                            <input
                                type="file"
                                accept=".pdf"
                                className="form-control"
                                onChange={handleFileChange}
                                required
                            />

                            <div className="form-text">
                                Chỉ chấp nhận file PDF.
                            </div>

                            {chapter.pdfFile && (
                                <div className="alert alert-info mt-3">
                                    <strong>Selected file:</strong>{" "}
                                    {chapter.pdfFile.name}
                                </div>
                            )}
                        </div>

                        <div className="d-flex gap-2">

                            <button
                                type="submit"
                                className="btn btn-success"
                            >
                                Upload Chapter
                            </button>

                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() =>
                                    navigate(`/manga/${id}`)
                                }
                            >
                                Cancel
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}