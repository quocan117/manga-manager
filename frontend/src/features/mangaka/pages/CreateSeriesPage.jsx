import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createSeriesWithCoverUpload } from "../../../services/mangakaService";

export default function CreateSeriesPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: "",
    genres: "",
    description: "",
    publicationType: "",
    artStyle: "",
  });

  const [coverImageFile, setCoverImageFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
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
    try {
      setLoading(true);
      const genres = form.genres
        .split(",")
        .map((g) => g.trim())
        .filter(Boolean);
      await createSeriesWithCoverUpload({ ...form, genres }, coverImageFile);
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
              <input
                type="text"
                name="genres"
                className="form-control"
                placeholder="Action, Adventure, Fantasy"
                value={form.genres}
                onChange={handleChange}
                required
              />
              <small className="text-muted">Ngăn cách bằng dấu phẩy</small>
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