import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { createChapter } from "../../services/mangakaService";
// Nhúng thêm chapterService để gọi hàm upload ảnh
import { chapterService } from "../../services/chapterService";

export default function CreateChapterPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  // Gộp chung state thông tin chapter và state quản lý file
  const [form, setForm] = useState({
    chapterNumber: "",
    title: "",
  });
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    setSelectedFiles(files);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Validate bắt buộc phải có ảnh
    if (!selectedFiles || selectedFiles.length === 0) {
      alert("Vui lòng chọn ít nhất 1 trang truyện!");
      return;
    }

    try {
      setLoading(true);

      // BƯỚC 1: Gọi API tạo Chapter trước
      const chapterResponse = await createChapter({
        seriesId: Number(id),
        chapterNumber: Number(form.chapterNumber),
        title: form.title,
      });

      // Lấy ID của chapter vừa được tạo ra
      const newChapterId = chapterResponse.id || chapterResponse.data?.id;

      // BƯỚC 2: Dùng ID đó để gọi tiếp API upload ảnh
      await chapterService.uploadChapterPages(newChapterId, selectedFiles);

      alert("Tạo chapter và tải ảnh thành công!");
      navigate("/mangaka/manga");
    } catch (error) {
      console.error(error);
      alert(
        "Có lỗi xảy ra khi tạo chapter hoặc tải ảnh. Vui lòng kiểm tra lại!",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-4">
      <div className="card shadow mx-auto" style={{ maxWidth: "700px" }}>
        <div className="card-body">
          <h3 className="mb-4">Create Chapter & Upload Pages</h3>

          <form onSubmit={handleSubmit}>
            {/* Input Chapter Number */}
            <div className="mb-3">
              <label>Chapter Number</label>
              <input
                type="number"
                name="chapterNumber"
                className="form-control"
                value={form.chapterNumber}
                onChange={handleChange}
                required
              />
            </div>

            {/* Input Title */}
            <div className="mb-3">
              <label>Title</label>
              <input
                type="text"
                name="title"
                className="form-control"
                value={form.title}
                onChange={handleChange}
                required
              />
            </div>

            {/* Khu vực Upload Ảnh được gộp vào */}
            <div className="mb-4 p-3 border rounded bg-light">
              <label className="fw-bold mb-2">Tải lên các trang truyện</label>
              <input
                type="file"
                multiple
                accept="image/png, image/jpeg, image/webp"
                onChange={handleFileChange}
                className="form-control mb-2"
                disabled={loading}
              />

              {/* Hiển thị tóm tắt số lượng ảnh đã chọn */}
              {selectedFiles.length > 0 && (
                <div className="text-muted small">
                  <p className="mb-1 font-semibold">
                    Đã chọn {selectedFiles.length} tệp:
                  </p>
                  <ul
                    className="list-unstyled ps-3 mb-0"
                    style={{ maxHeight: "100px", overflowY: "auto" }}
                  >
                    {selectedFiles.map((file, index) => (
                      <li key={index}>
                        📄 {file.name} ({(file.size / 1024 / 1024).toFixed(2)}{" "}
                        MB)
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>

            {/* Nút Submit */}
            <div className="d-flex gap-2">
              <button
                type="submit"
                className="btn btn-success"
                disabled={loading || selectedFiles.length === 0}
              >
                {loading ? "Đang xử lý..." : "Lưu & Tải Ảnh Lên"}
              </button>

              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => navigate("/mangaka/manga")}
                disabled={loading}
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
