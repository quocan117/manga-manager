import { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getChapterPages,
  getChapterById,
} from "../../../services/mangakaService";
import { chapterService } from "../../../services/chapterService";
import { resolveImageUrl } from "../../../utils/imageUrl";
import api from "../../../services/api";

export default function ChapterPages() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [chapter, setChapter] = useState(null);
  const [newFiles, setNewFiles] = useState([]);
  const [uploading, setUploading] = useState(false);
  const fileInputRefs = useRef({});

  const fetchData = async () => {
    try {
      setLoading(true);
      const [chapterData, pagesData] = await Promise.all([
        getChapterById(chapterId),
        getChapterPages(chapterId),
      ]);
      setChapter(chapterData);
      setPages(pagesData || []);
    } catch (error) {
      console.error("Lỗi khi tải dữ liệu chapter:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [chapterId]);

  const handleNewFileChange = (e) => {
    setNewFiles(Array.from(e.target.files));
  };

  const handleUploadMore = async () => {
    if (!newFiles || newFiles.length === 0)
      return alert("Vui lòng chọn ít nhất 1 ảnh!");
    try {
      setUploading(true);
      await chapterService.uploadChapterPages(chapterId, newFiles);
      setNewFiles([]);
      await fetchData();
      alert("Đã thêm trang truyện mới thành công!");
    } catch (error) {
      console.error("Lỗi khi tải thêm trang:", error);
      alert("Có lỗi xảy ra khi tải thêm trang.");
    } finally {
      setUploading(false);
    }
  };

  const handleReplacePageImage = async (pageId, file) => {
    if (!file) return;
    try {
      setUploading(true);
      const formData = new FormData();
      formData.append("image", file);
      await api.put(`/mangaka/pages/${pageId}/image`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      alert("Đã cập nhật ảnh mới cho trang thành công!");
      fetchData();
    } catch (error) {
      console.error(error);
      alert(error?.response?.data?.message || "Lỗi khi cập nhật ảnh trang.");
    } finally {
      setUploading(false);
    }
  };

  const handleDeletePage = async (pageId, pageNum) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa trang ${pageNum} không?`)) {
      try {
        await api.delete(`/mangaka/pages/${pageId}`);
        alert("Đã xóa trang thành công!");
        fetchData();
      } catch (error) {
        console.error(error);
        alert(
          error?.response?.data?.message ||
            "Lỗi khi xóa trang! Vui lòng thử lại.",
        );
      }
    }
  };

  if (loading)
    return <div className="text-center mt-5">Đang tải danh sách trang...</div>;

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Quản lý các trang - Chapter {chapter?.chapterNumber}</h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Quay lại
        </button>
      </div>

      <div className="mb-4 p-4 border rounded bg-white shadow-sm">
        <label className="fw-bold mb-3 d-block text-primary">
          ➕ Thêm trang truyện (Upload ảnh nối tiếp)
        </label>
        <input
          key={newFiles.length === 0 ? "empty" : "has-files"}
          type="file"
          multiple
          accept="image/png, image/jpeg, image/webp"
          onChange={handleNewFileChange}
          className="form-control mb-3"
          disabled={uploading}
        />
        <button
          className="btn btn-success fw-bold px-4"
          onClick={handleUploadMore}
          disabled={uploading || newFiles.length === 0}
        >
          {uploading
            ? "Đang tải lên..."
            : `Tải lên (tiếp nối từ trang ${pages.length + 1})`}
        </button>
      </div>

      <div className="row g-4">
        {pages.length === 0 ? (
          <div className="col-12 text-center text-muted py-5 bg-white rounded shadow-sm">
            <div className="display-1 opacity-25 mb-3">🖼️</div>
            <p className="mb-0">
              Chapter này chưa có ảnh trang nào. Vui lòng upload ảnh để bắt đầu.
            </p>
          </div>
        ) : (
          pages.map((page, index) => {
            const isFinalized =
              page.pageStatus === "DRAWING_FINALIZED" ||
              page.status === "DRAWING_FINALIZED";
            return (
              <div className="col-md-3" key={page.id}>
                <div
                  className={`card shadow-sm h-100 ${isFinalized ? "border-success border-2" : ""}`}
                >
                  <div className="position-relative">
                    <img
                      src={resolveImageUrl(
                        page.imageUrl,
                        "https://placehold.co/200x300?text=Trang+" +
                          page.pageNumber,
                      )}
                      className="card-img-top"
                      alt={`Page ${page.pageNumber}`}
                      style={{ height: "300px", objectFit: "cover" }}
                    />
                    <input
                      type="file"
                      accept="image/*"
                      style={{ display: "none" }}
                      ref={(el) => (fileInputRefs.current[page.id] = el)}
                      onChange={(e) =>
                        handleReplacePageImage(page.id, e.target.files[0])
                      }
                    />
                    <button
                      className="btn btn-sm btn-dark position-absolute top-0 end-0 m-2"
                      title="Upload ảnh đè lên trang này để vẽ tiếp"
                      onClick={() => fileInputRefs.current[page.id].click()}
                      disabled={uploading}
                    >
                      Đổi ảnh
                    </button>
                  </div>

                  <div className="card-body text-center d-flex flex-column">
                    <h5 className="card-title fw-bold">
                      Trang {page.pageNumber}
                    </h5>
                    <span
                      className={`badge mb-3 ${isFinalized ? "bg-success" : "bg-secondary"}`}
                    >
                      {isFinalized ? "Đã Chốt" : page.pageStatus || "Bản nháp"}
                    </span>
                    <div className="mt-auto d-flex gap-2">
                      <button
                        className="btn btn-primary flex-grow-1 btn-sm fw-bold shadow-sm"
                        onClick={() =>
                          navigate(`/mangaka/pages/${page.id}/drawing`, {
                            state: {
                              originalImageUrl: page.imageUrl,
                              pageNumber: page.pageNumber,
                            },
                          })
                        }
                      >
                        Thao tác
                      </button>
                      <button
                        className="btn btn-outline-danger btn-sm shadow-sm fw-bold px-3"
                        title="Xóa trang này"
                        onClick={() =>
                          handleDeletePage(page.id, page.pageNumber)
                        }
                      >
                        Xóa
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
