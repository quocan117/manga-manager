import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createSeriesWithCoverUpload } from "../../../services/mangakaService";
import { GENRE_OPTIONS } from "../../../constants/genres";

export default function CreateSeriesPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: "",
    genres: [], 
    description: "",
    publicationType: "",
    artStyle: "",
  });
  const [showGenreDropdown, setShowGenreDropdown] = useState(false);
  const [coverImageFile, setCoverImageFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const toggleGenre = (genre) => {
    setForm((prev) => {
      const isSelected = prev.genres.includes(genre);
      return {
        ...prev,
        genres: isSelected
          ? prev.genres.filter((g) => g !== genre)
          : [...prev.genres, genre],
      };
    });
  };

  const removeGenre = (genre) => {
    setForm((prev) => ({
      ...prev,
      genres: prev.genres.filter((g) => g !== genre),
    }));
  };

  const handleCoverFileChange = (e) => {
    const file = e.target.files[0] || null;
    setCoverImageFile(file);
    setCoverPreview(file ? URL.createObjectURL(file) : null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!coverImageFile) {
      alert("Vui lòng chọn ảnh bìa cho series!");
      return;
    }
    if (form.genres.length === 0) {
      alert("Vui lòng chọn ít nhất một thể loại!");
      return;
    }
    try {
      setLoading(true);
      await createSeriesWithCoverUpload({ ...form }, coverImageFile);
      alert("Tạo Series thành công!");
      navigate("/mangaka/manga");
    } catch (error) {
      console.error(error);
      alert(
        "Không thể tạo Series: " +
          (error.response?.data?.message || "Vui lòng kiểm tra lại thông tin"),
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container">
      <div className="card shadow">
        <div className="card-header">
          <h3 className="mb-0">Create New Series</h3>
        </div>
        <div className="card-body">
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Title</label>
              <input
                type="text"
                name="title"
                className="form-control"
                value={form.title}
                onChange={handleChange}
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label">Genres</label>

              <div className="position-relative">
                <button
                  type="button"
                  className="form-select text-start"
                  onClick={() => setShowGenreDropdown((prev) => !prev)}
                >
                  {form.genres.length > 0
                    ? `${form.genres.length} thể loại đã chọn`
                    : "-- Chọn thể loại --"}
                </button>

                {showGenreDropdown && (
                  <div
                    className="border rounded shadow-sm bg-white p-2 position-absolute w-100"
                    style={{
                      zIndex: 1000,
                      maxHeight: "220px",
                      overflowY: "auto",
                    }}
                  >
                    {GENRE_OPTIONS.map((genre) => (
                      <div className="form-check" key={genre}>
                        <input
                          type="checkbox"
                          className="form-check-input"
                          id={`genre-${genre}`}
                          checked={form.genres.includes(genre)}
                          onChange={() => toggleGenre(genre)}
                        />
                        <label
                          className="form-check-label"
                          htmlFor={`genre-${genre}`}
                        >
                          {genre}
                        </label>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {form.genres.length > 0 && (
                <div className="mt-2 d-flex flex-wrap gap-2">
                  {form.genres.map((genre) => (
                    <span key={genre} className="badge bg-secondary">
                      {genre}{" "}
                      <button
                        type="button"
                        onClick={() => removeGenre(genre)}
                        className="btn-close btn-close-white btn-sm ms-1"
                        style={{ fontSize: "0.55rem" }}
                        aria-label={`Remove ${genre}`}
                      />
                    </span>
                  ))}
                </div>
              )}
              <small className="text-muted d-block mt-1">
                Chỉ chọn trong danh sách thể loại cố định để độc giả có thể tìm
                thấy series khi lọc theo thể loại ở trang chủ.
              </small>
            </div>

            <div className="mb-3 p-3 border rounded bg-light">
              <label className="form-label fw-bold">Ảnh bìa series</label>
              <input
                type="file"
                accept="image/png, image/jpeg, image/webp, image/gif"
                className="form-control mb-2"
                onChange={handleCoverFileChange}
                disabled={loading}
                required
              />
              <small className="text-muted d-block">
                Tối đa 5MB. Định dạng: JPG, PNG, WEBP, GIF.
              </small>
            </div>
            {coverPreview && (
              <div className="mb-3">
                <img
                  src={coverPreview}
                  alt="Preview"
                  className="img-thumbnail"
                  style={{ maxHeight: "250px" }}
                />
              </div>
            )}
            <div className="mb-3">
              <label className="form-label">Description</label>
              <textarea
                rows="5"
                name="description"
                className="form-control"
                value={form.description}
                onChange={handleChange}
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Publication Type</label>
              <select
                name="publicationType"
                className="form-select"
                value={form.publicationType}
                onChange={handleChange}
              >
                <option value="">Select...</option>
                <option value="Manga">Manga</option>
                <option value="Manhwa">Manhwa</option>
                <option value="Manhua">Manhua</option>
                <option value="Comic">Comic</option>
              </select>
            </div>
            <div className="mb-4">
              <label className="form-label">Art Style</label>
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
                {loading ? "Creating..." : "Create Series"}
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => navigate("/mangaka/manga")}
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
