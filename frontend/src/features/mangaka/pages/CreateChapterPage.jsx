import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { chapterService } from "../../../services/chapterService";

export default function CreateChapterPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    chapterNumber: "",
    title: "",
    expectedPages: "",
  });

  const [pageFiles, setPageFiles] = useState({});
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
    setErrorMsg("");
  };

  const handleSingleFileChange = (index, file) => {
    setPageFiles((prev) => ({
      ...prev,
      [index]: file,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const totalPages = Number(form.expectedPages);
    if (!totalPages || totalPages <= 0) {
      setErrorMsg("Vui lòng nhập số lượng trang dự kiến hợp lệ!");
      return;
    }
    const filesArray = [];
    for (let i = 0; i < totalPages; i++) {
      if (!pageFiles[i]) {
        setErrorMsg(`Vui lòng chọn ảnh cho Trang ${i + 1}!`);
        return;
      }
      filesArray.push(pageFiles[i]);
    }

    try {
      setLoading(true);
      const finalTitle = `Chapter ${form.chapterNumber}: ${form.title.trim()}`;
      const chapterResponse = await chapterService.createChapter({
        seriesId: Number(id),
        chapterNumber: Number(form.chapterNumber),
        title: finalTitle, 
        expectedPages: totalPages,
      });
      const newChapterId = chapterResponse.id || chapterResponse.chapterId;
      if (newChapterId) {
        await chapterService.uploadChapterPages(newChapterId, filesArray);
      }

      alert("Tạo chapter và tải lên từng trang thành công!");
      navigate("/mangaka/manga");
    } catch (error) {
      console.error(error);
      const backendMsg =
        error?.response?.data?.message || error?.response?.data?.error;
      if (error?.response?.status === 409) {
        setErrorMsg(
          "Lỗi: Số Chapter này đã tồn tại trong truyện. Vui lòng nhập số khác!",
        );
      } else {
        setErrorMsg(
          backendMsg ||
            "Có lỗi xảy ra khi tạo chapter hoặc tải ảnh lên. Vui lòng kiểm tra lại!",
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const numPages = parseInt(form.expectedPages) || 0;

  return (
    <div className="container mt-4 mb-5">
      <div className="card shadow mx-auto" style={{ maxWidth: "800px" }}>
        <div className="card-body p-4">
          <h3 className="mb-4 text-primary fw-bold">
            Tạo Chapter & Tải Ảnh Từng Trang
          </h3>

          {errorMsg && (
            <div className="alert alert-danger fw-bold shadow-sm d-flex align-items-center">
              <i className="fas fa-exclamation-triangle me-2"></i>
              {errorMsg}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label fw-bold">
                Số Chapter <span className="text-danger">*</span>
              </label>
              <input
                type="number"
                name="chapterNumber"
                className="form-control"
                value={form.chapterNumber}
                onChange={handleChange}
                required
              />
            </div>

            <div className="mb-3">
              <label className="form-label fw-bold">
                Tiêu đề Chapter <span className="text-danger">*</span>
              </label>
              <div className="input-group">
                <span className="input-group-text bg-light text-secondary fw-bold border-end-0">
                  Chapter {form.chapterNumber || "X"}:
                </span>
                <input
                  type="text"
                  name="title"
                  className="form-control"
                  placeholder="Nhập nội dung tiêu đề... (VD: Bình minh của cuộc phiêu lưu)"
                  value={form.title}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="mb-4">
              <label className="form-label fw-bold text-primary">
                Số lượng trang dự kiến <span className="text-danger">*</span>
              </label>
              <input
                type="number"
                min="1"
                max="100"
                name="expectedPages"
                className="form-control border-primary"
                placeholder="Ví dụ: 5 (Hệ thống sẽ hiện ra 5 ô upload tương ứng)"
                value={form.expectedPages}
                onChange={handleChange}
                required
              />
              <small className="text-muted d-block mt-1">
                Khi bạn nhập số trang, các ô chọn file tương ứng cho từng trang
                sẽ tự động xuất hiện bên dưới.
              </small>
            </div>

            {numPages > 0 && (
              <div className="mb-4 p-3 border border-primary border-opacity-25 rounded bg-light">
                <label className="fw-bold text-primary mb-3 d-block">
                  📄 Chọn ảnh cho từng trang cụ thể:
                </label>
                <div
                  className="d-flex flex-column gap-3"
                  style={{ maxHeight: "400px", overflowY: "auto" }}
                >
                  {Array.from({ length: numPages }, (_, index) => (
                    <div
                      key={index}
                      className="p-3 bg-white border rounded shadow-sm d-flex align-items-center justify-content-between flex-wrap gap-2"
                    >
                      <div style={{ minWidth: "90px" }}>
                        <span className="badge bg-primary px-3 py-2 fs-6">
                          Trang {index + 1}
                        </span>
                      </div>
                      <div className="flex-grow-1">
                        <input
                          type="file"
                          accept="image/png, image/jpeg, image/webp"
                          onChange={(e) =>
                            handleSingleFileChange(index, e.target.files[0])
                          }
                          className="form-control form-control-sm"
                          required
                        />
                      </div>
                      {pageFiles[index] && (
                        <span
                          className="badge bg-success text-wrap"
                          style={{ maxWidth: "180px" }}
                        >
                          {pageFiles[index].name}
                        </span>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="d-flex gap-2 mt-4">
              <button
                type="submit"
                className="btn btn-success fw-bold px-4 shadow-sm"
                disabled={loading || numPages <= 0}
              >
                {loading
                  ? "Đang xử lý & tải ảnh..."
                  : "Tạo Chapter & Tải Ảnh Lên"}
              </button>
              <button
                type="button"
                className="btn btn-secondary px-4"
                onClick={() => navigate("/mangaka/manga")}
                disabled={loading}
              >
                Hủy
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
