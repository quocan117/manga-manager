import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ChapterMarkupUploader from "../../../components/ChapterMarkupUploader";
import { resolveImageUrl } from "../../../utils/imageUrl";
import {
  getChapterForReview,
  saveChapterRevisionNote,
  sendChapterRevisionToMangaka,
  approveAndReadyChapter,
} from "../../../services/chapterEditorService";
export default function ChapterReviewPage() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  useEffect(() => {
    fetchChapter();
  }, [chapterId]);
  const fetchChapter = async () => {
    try {
      setLoading(true);
      const data = await getChapterForReview(chapterId);
      setChapter(data);
    } catch (error) {
      console.error("Lỗi tải chapter:", error);
    } finally {
      setLoading(false);
    }
  };
  const handleSubmitToPublish = async () => {
    if (
      !window.confirm(
        "Xác nhận phê duyệt Chapter này? Chapter sẽ được đưa vào hàng đợi xuất bản theo lịch của Series.",
      )
    )
      return;
    try {
      setSubmitting(true);
      await approveAndReadyChapter(chapterId);
      alert("Đã phê duyệt Chapter thành công!");
      navigate("/tantou");
    } catch (error) {
      console.error(error);
      alert("Lỗi khi phê duyệt chapter.");
    } finally {
      setSubmitting(false);
    }
  };
  const handleSaveNote = async (
    index,
    { canvasData, previewImageUrl, description },
  ) => {
    return saveChapterRevisionNote(chapterId, {
      previewImageUrl,
      canvasData,
      description,
      orderIndex: index,
    });
  };
  const handleSendAll = async () => {
    try {
      await sendChapterRevisionToMangaka(chapterId);
      alert("Đã gửi các ảnh đánh dấu yêu cầu chỉnh sửa về cho Mangaka!");
      navigate("/tantou");
    } catch (error) {
      console.error(error);
      alert("Lỗi khi gửi yêu cầu chỉnh sửa.");
    }
  };
  if (loading) return <div className="p-4">Đang tải chapter...</div>;
  const pages = [...(chapter?.pages || [])].sort(
    (a, b) => (a.pageNumber ?? 0) - (b.pageNumber ?? 0),
  );
  // Xếp các trang đúng theo vị trí pageNumber (1, 2, 3...), trang nào
  // chưa có dữ liệu thì hiển thị placeholder thay vì bị "nhảy cóc".
  const maxPageNumber = pages.reduce(
    (max, page) => Math.max(max, page.pageNumber ?? 0),
    0,
  );
  const pageSlots = Array.from({ length: maxPageNumber }, (_, i) => {
    const pageNumber = i + 1;
    return pages.find((page) => page.pageNumber === pageNumber) || null;
  });
  const handleDownload = async (url, fileName) => {
    try {
      const response = await fetch(url);
      const blob = await response.blob();
      const blobUrl = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = blobUrl;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(blobUrl);
    } catch (error) {
      console.error("Lỗi khi tải ảnh:", error);
      alert("Không thể tải ảnh trang này.");
    }
  };
  return (
    <div className="p-4 bg-light min-vh-100">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          {chapter?.seriesTitle && (
            <div className="text-muted fs-6 mb-1">{chapter.seriesTitle}</div>
          )}
          Duyệt {`${chapter.title}`}
        </h2>
        <button
          className="btn btn-secondary"
          onClick={() => navigate("/tantou")}
        >
          Trở về
        </button>
      </div>
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-header bg-white fw-bold d-flex justify-content-between align-items-center">
          <span>Các trang truyện Mangaka đã nộp</span>
          <span className="badge bg-success">{pages.length} trang</span>
        </div>
        <div className="card-body">
          {pageSlots.length === 0 ? (
            <p className="text-muted mb-0">
              Chapter này chưa có trang nào được gửi.
            </p>
          ) : (
            <div className="row g-3">
              {pageSlots.map((page, i) => {
                const pageNumber = i + 1;
                if (!page || !page.imageUrl) {
                  return (
                    <div className="col-6 col-md-3" key={`empty-${pageNumber}`}>
                      <div className="border rounded overflow-hidden h-100 d-flex flex-column">
                        <div
                          className="d-flex align-items-center justify-content-center bg-light text-muted flex-grow-1"
                          style={{ aspectRatio: "3/4" }}
                        >
                          Chưa có trang
                        </div>
                        <div className="p-2 small text-muted">
                          Trang {pageNumber}
                        </div>
                      </div>
                    </div>
                  );
                }
                const resolvedUrl = resolveImageUrl(page.imageUrl);
                return (
                  <div className="col-6 col-md-3" key={page.id}>
                    <div className="border rounded overflow-hidden h-100">
                      <a
                        href={resolvedUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                      >
                        <img
                          src={resolvedUrl}
                          alt={`Trang ${page.pageNumber}`}
                          className="w-100"
                          style={{ objectFit: "cover", aspectRatio: "3/4" }}
                        />
                      </a>
                      <div className="p-2 d-flex justify-content-between align-items-center small">
                        <span>Trang {page.pageNumber}</span>
                        <span className="badge bg-secondary">
                          {page.status}
                        </span>
                      </div>
                      <div className="p-2 pt-0">
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-primary w-100"
                          onClick={() =>
                            handleDownload(
                              resolvedUrl,
                              `trang-${page.pageNumber}${
                                resolvedUrl.includes(".")
                                  ? resolvedUrl.slice(
                                      resolvedUrl.lastIndexOf("."),
                                    )
                                  : ""
                              }`,
                            )
                          }
                        >
                          Tải xuống
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-header bg-white fw-bold">
          Đánh dấu các trang cần chỉnh sửa
        </div>
        <div className="card-body">
          <p className="text-muted small">
            Tải ảnh cần sửa từ file gốc vừa mở về máy, upload từng ảnh lên đây,
            đánh dấu bằng công cụ vẽ rồi bấm "Lưu đánh dấu" để chốt, sau đó gửi
            toàn bộ cho Mangaka.
          </p>
          <ChapterMarkupUploader
            onSaveNote={handleSaveNote}
            onSendAll={handleSendAll}
          />
        </div>
      </div>
      <div className="card shadow-sm border-0">
        <div className="card-header bg-white fw-bold">
          Quyết định của Biên tập viên
        </div>
        <div className="card-body">
          <button
            className="btn btn-success fw-bold"
            onClick={handleSubmitToPublish}
            disabled={submitting}
          >
            {submitting ? "Đang xử lý..." : "Phê duyệt & Chờ xuất bản"}
          </button>
        </div>
      </div>
    </div>
  );
}
