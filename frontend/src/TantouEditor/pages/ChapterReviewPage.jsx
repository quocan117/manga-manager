import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ChapterMarkupUploader from "../../components/ChapterMarkupUploader";
import {
  getChapterForReview,
  saveChapterRevisionNote,
  sendChapterRevisionToMangaka,
  approveChapter,
} from "../../services/chapterEditorService";
export default function ChapterReviewPage() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState(null);
  const [loading, setLoading] = useState(true);
  const [approving, setApproving] = useState(false);
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
  const handleSaveNote = async (index, { canvasData, previewImageUrl }) => {
    return saveChapterRevisionNote(chapterId, {
      previewImageUrl,
      canvasData,
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

  const handleApprove = async () => {
    if (
      !window.confirm(
        "Xác nhận duyệt chapter này? Chapter sẽ tự động xuất bản theo lịch đã cấu hình cho series.",
      )
    )
      return;
    try {
      setApproving(true);
      await approveChapter(chapterId);
      alert(
        "Đã duyệt chapter! Mangaka đã được thông báo, chapter sẽ tự động xuất bản theo lịch.",
      );
      navigate("/tantou");
    } catch (error) {
      console.error(error);
      alert("Lỗi khi duyệt chapter.");
    } finally {
      setApproving(false);
    }
  };

  if (loading) return <div className="p-4">Đang tải chapter...</div>;

  return (
    <div className="p-4 bg-light min-vh-100">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          Duyệt Chapter {chapter?.chapterNumber ?? chapterId}
          {chapter?.title ? `: ${chapter.title}` : ""}
        </h2>
        <button
          className="btn btn-secondary"
          onClick={() => navigate("/tantou")}
        >
          Trở về
        </button>
      </div>
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-header bg-white fw-bold">
          File gốc do Mangaka gửi
        </div>
        <div className="card-body">
          {chapter?.manuscriptUrl ? (
            <a
              href={chapter.manuscriptUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-outline-primary"
            >
              🔗 Mở link file gốc để tải về kiểm tra
            </a>
          ) : (
            <p className="text-muted mb-0">Chưa có link file gốc.</p>
          )}
        </div>
      </div>
      <div className="card shadow-sm border-0 mb-4">
        <div className="card-header bg-white fw-bold">
          Đánh dấu các trang cần chỉnh sửa (nếu có)
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
        <div className="card-header bg-white fw-bold">Duyệt Chapter</div>
        <div className="card-body">
          <p className="text-muted">
            Nếu chapter đã đạt yêu cầu, không cần chỉnh sửa gì thêm, hãy duyệt
            để Mangaka được thông báo. Chapter sẽ{" "}
            <strong>tự động xuất bản</strong> theo lịch đã cấu hình ở mục{" "}
            <strong>Lịch Xuất Bản</strong> — không xuất bản ngay lập tức.
          </p>
          <button
            className="btn btn-success fw-bold"
            onClick={handleApprove}
            disabled={approving}
          >
            {approving ? "Đang duyệt..." : "✅ Duyệt Chapter"}
          </button>
        </div>
      </div>
    </div>
  );
}
