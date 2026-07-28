import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import ChapterMarkupUploader from "../../../components/ChapterMarkupUploader";
import SeriesFileList from "../../../components/SeriesFileList";
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

  return (
    <div className="p-4 bg-light min-vh-100">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          {chapter?.seriesTitle && (
            <div className="text-muted fs-6 mb-1">{chapter.seriesTitle}</div>
          )}
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
          <SeriesFileList
            files={chapter?.manuscriptFiles || []}
            emptyText="Chưa có file bản thảo nào."
          />
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
        <div className="card-header bg-white fw-bold">
          Quyết định của Biên tập viên
        </div>
        <div className="card-body">
          <button
            className="btn btn-success fw-bold"
            onClick={handleSubmitToPublish}
            disabled={submitting}
          >
            {submitting ? "Đang xử lý..." : "✅ Phê duyệt & Chờ xuất bản"}
          </button>
        </div>
      </div>
    </div>
  );
}
