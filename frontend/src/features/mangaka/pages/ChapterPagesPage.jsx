import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getChapterPages, getChapterById } from "../../../services/mangakaService";
import { chapterService } from "../../../services/chapterService";
import { resolveImageUrl } from "../../../utils/imageUrl";

export default function ChapterPages() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [chapter, setChapter] = useState(null);
  const [newFiles, setNewFiles] = useState([]);
  const [uploading, setUploading] = useState(false);

  const fetchData = async () => {
    try {
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
    if (!newFiles || newFiles.length === 0) {
      alert("Vui lòng chọn ít nhất 1 ảnh để thêm trang!");
      return;
    }
    try {
      setUploading(true);
      await chapterService.uploadChapterPages(chapterId, newFiles);
      setNewFiles([]);
      await fetchData();
      alert("Đã thêm trang truyện mới thành công!");
    } catch (error) {
      console.error("Lỗi khi tải thêm trang:", error);
      alert("Có lỗi xảy ra khi tải thêm trang. Vui lòng thử lại!");
    } finally {
      setUploading(false);
    }
  };

  if (loading)
    return <div className="text-center mt-5">Đang tải danh sách trang...</div>;
  
  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          Quản lý các trang - Chapter {chapter?.chapterNumber ?? "?"}
          {chapter?.title ? `: ${chapter.title}` : ""}
        </h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Quay lại
        </button>
      </div>
      <div className="mb-4 p-3 border rounded bg-light">
        <label className="fw-bold mb-2 d-block">
          ➕ Thêm trang truyện mới cho chapter này
        </label>
        <input
          key={newFiles.length === 0 ? "empty" : "has-files"}
          type="file"
          multiple
          accept="image/png, image/jpeg, image/webp"
          onChange={handleNewFileChange}
          className="form-control mb-2"
          disabled={uploading}
        />
        {newFiles.length > 0 && (
          <div className="text-muted small mb-2">
            <p className="mb-1">Đã chọn {newFiles.length} tệp:</p>
            <ul
              className="list-unstyled ps-3 mb-0"
              style={{ maxHeight: "100px", overflowY: "auto" }}
            >
              {newFiles.map((file, index) => (
                <li key={index}>
                  📄 {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                </li>
              ))}
            </ul>
          </div>
        )}
        <button
          className="btn btn-success"
          onClick={handleUploadMore}
          disabled={uploading || newFiles.length === 0}
        >
          {uploading
            ? "Đang tải lên..."
            : `Tải lên (sẽ thêm từ trang ${pages.length + 1})`}
        </button>
      </div>
      <div className="row">
        {pages.length === 0 ? (
          <div className="col-12 text-center text-muted">
            Chapter này chưa có trang nào.
          </div>
        ) : (
          pages.map((page) => (
            <div className="col-md-3 mb-4" key={page.id}>
              <div className="card shadow-sm">
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
                <div className="card-body text-center">
                  <h5 className="card-title">Trang {page.pageNumber}</h5>
                  <span className="badge bg-secondary mb-3">{page.status}</span>
                  <br />
                  <button
                    className="btn btn-primary w-100"
                    onClick={() =>
                      navigate(`/mangaka/pages/${page.id}/drawing`, {
                        state: { originalImageUrl: page.imageUrl },
                      })
                    }
                  >
                    🖌️ Chỉnh sửa / Đánh dấu
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}