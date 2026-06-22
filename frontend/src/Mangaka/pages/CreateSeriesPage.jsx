import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createSeries } from "../../services/mangakaService";

export default function CreateSeriesPage() {

    const navigate = useNavigate();

    const [form, setForm] = useState({
        title: "",
        genres: "",
        coverUrl: "",
        description: "",
        publicationType: "",
        artStyle: ""
    });

    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {

        setForm({
            ...form,
            [e.target.name]: e.target.value
        });

    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            setLoading(true);

            const payload = {
                title: form.title,
                genres: form.genres
                    .split(",")
                    .map(g => g.trim())
                    .filter(Boolean),

                coverUrl: form.coverUrl,
                description: form.description,
                publicationType: form.publicationType,
                artStyle: form.artStyle
            };

            await createSeries(payload);

            alert("Tạo Series thành công!");

            navigate("/mangaka/manga");

        } catch (error) {

            console.error(error);

            alert("Không thể tạo Series");

        } finally {

            setLoading(false);

        }

    };

    return (

        <div className="container">

            <div className="card shadow">

                <div className="card-header">
                    <h3 className="mb-0">
                        Create New Series
                    </h3>
                </div>

                <div className="card-body">

                    <form onSubmit={handleSubmit}>

                        {/* Title */}

                        <div className="mb-3">

                            <label className="form-label">
                                Title
                            </label>

                            <input
                                type="text"
                                name="title"
                                className="form-control"
                                value={form.title}
                                onChange={handleChange}
                                required
                            />

                        </div>

                        {/* Genres */}

                        <div className="mb-3">

                            <label className="form-label">
                                Genres
                            </label>

                            <input
                                type="text"
                                name="genres"
                                className="form-control"
                                placeholder="Action, Adventure, Fantasy"
                                value={form.genres}
                                onChange={handleChange}
                                required
                            />

                            <small className="text-muted">
                                Ngăn cách bằng dấu phẩy
                            </small>

                        </div>

                        {/* Cover */}

                        <div className="mb-3">

                            <label className="form-label">
                                Cover URL
                            </label>

                            <input
                                type="text"
                                name="coverUrl"
                                className="form-control"
                                value={form.coverUrl}
                                onChange={handleChange}
                            />

                        </div>

                        {/* Preview */}

                        {form.coverUrl && (

                            <div className="mb-3">

                                <img
                                    src={form.coverUrl}
                                    alt="Preview"
                                    className="img-thumbnail"
                                    style={{
                                        maxHeight: "250px"
                                    }}
                                />

                            </div>

                        )}

                        {/* Description */}

                        <div className="mb-3">

                            <label className="form-label">
                                Description
                            </label>

                            <textarea
                                rows="5"
                                name="description"
                                className="form-control"
                                value={form.description}
                                onChange={handleChange}
                            />

                        </div>

                        {/* Publication Type */}

                        <div className="mb-3">

                            <label className="form-label">
                                Publication Type
                            </label>

                            <select
                                name="publicationType"
                                className="form-select"
                                value={form.publicationType}
                                onChange={handleChange}
                            >

                                <option value="">
                                    Select...
                                </option>

                                <option value="Manga">
                                    Manga
                                </option>

                                <option value="Manhwa">
                                    Manhwa
                                </option>

                                <option value="Manhua">
                                    Manhua
                                </option>

                                <option value="Comic">
                                    Comic
                                </option>

                            </select>

                        </div>

                        {/* Art Style */}

                        <div className="mb-4">

                            <label className="form-label">
                                Art Style
                            </label>

                            <input
                                type="text"
                                name="artStyle"
                                className="form-control"
                                placeholder="Shonen, Seinen, Chibi..."
                                value={form.artStyle}
                                onChange={handleChange}
                            />

                        </div>

                        <div className="d-flex gap-2">

                            <button
                                type="submit"
                                className="btn btn-success"
                                disabled={loading}
                            >
                                {
                                    loading
                                        ? "Creating..."
                                        : "Create Series"
                                }
                            </button>

                            <button
                                type="button"
                                className="btn btn-secondary"
                                onClick={() =>
                                    navigate("/mangaka/manga")
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