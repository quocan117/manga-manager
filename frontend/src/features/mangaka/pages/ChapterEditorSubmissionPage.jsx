import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getChapterById } from "../../../services/mangakaService";
import { isValidHttpUrl } from "../../../utils/urlValidator";
import {
  submitChapterToEditor,
  getChapterRevisionNotes,
} from "../../../services/chapterEditorService";
import { resolveImageUrl } from "../../../utils/imageUrl";
import CanvasMarkupTool from "../../../components/CanvasMarkupTool";

const SUBMITTABLE_STATUSES = ["DRAFT", "REVISION_REQUESTED", undefined, null];

export default function ChapterEditorSubmission() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState(null);
  const [manuscriptUrl, setManuscriptUrl] = useState("");
  const [revisionNotes, setRevisionNotes] = useState([]);
  const [currentNoteIndex, setCurrentNoteIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchData();
  }, [chapterId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const chapterData = await getChapterById(chapterId);
      setChapter(chapterData);
      setManuscriptUrl(chapterData?.manuscriptUrl || "");
      if (chapterData?.status === "REVISION_REQUESTED") {
        const notes = await getChapterRevisionNotes(chapterId);
        const sortedNotes = [...(notes || [])].sort(
          (a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0),
        );
        setRevisionNotes(sortedNotes);
        setCurrentNoteIndex(0);
      } else {
        setRevisionNotes([]);
        setCurrentNoteIndex(0);
      }
    } catch (err) {
      console.error(err);
      setError("Không thể tải thông tin chapter.");
    } finally {
      setLoading(false);
    }
  };

  const isResubmit = chapter?.status === "REVISION_REQUESTED";
  const canSubmit = SUBMITTABLE_STATUSES.includes(chapter?.status);
  const currentNote = revisionNotes[currentNoteIndex];

  const handlePrevNote = () => {
    setCurrentNoteIndex((i) => Math.max(0, i - 1));
  };

  const handleNextNote = () => {
    setCurrentNoteIndex((i) => Math.min(revisionNotes.length - 1, i + 1));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!manuscriptUrl.trim()) {
      setError("Vui lòng nhập link file gốc (Google Drive,...) trước khi gửi.");
      return;
    }
    if (!isValidHttpUrl(manuscriptUrl)) {
      setError(
        "Link file gốc không hợp lệ. Vui lòng nhập đúng định dạng URL (bắt đầu bằng http:// hoặc https://).",
      );
      return;
    }
    try {
      setSubmitting(true);
      const updated = await submitChapterToEditor(
        chapterId,
        manuscriptUrl.trim(),
      );
      setChapter((prev) => ({ ...prev, ...updated }));
      alert("Đã gửi file gốc chapter cho Biên tập!");
    } catch (err) {
      console.error(err);
      setError(
        err?.response?.data?.message ||
          "Không thể gửi chapter lúc này. Vui lòng thử lại.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading)
    return <div className="text-center mt-5">Đang tải dữ liệu chapter...</div>;
  
  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          Gửi file gốc Chapter {chapter?.chapterNumber ?? "?"} cho Biên tập
        </h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Quay lại
        </button>
      </div>
      <div className="mb-3">
        <strong>Trạng thái hiện tại: </strong>
        <span className="badge bg-secondary">{chapter?.status || "DRAFT"}</span>
      </div>
      {isResubmit && (
        <div className="alert alert-warning">
          Biên tập đã yêu cầu chỉnh sửa chapter này. Xem các ảnh được đánh dấu
          bên dưới, chỉnh sửa lại file gốc, rồi gửi lại link mới cho biên tập.
        </div>
      )}
      {revisionNotes.length > 0 && currentNote && (
        <div className="card shadow-sm mb-4">
          <div className="card-header bg-white fw-bold d-flex justify-content-between align-items-center flex-wrap gap-2">
            <span>
              Các trang Biên tập yêu cầu chỉnh sửa ({revisionNotes.length})
            </span>
            <div className="d-flex align-items-center gap-2">
              <button
                type="button"
                className="btn btn-sm btn-outline-secondary"
                onClick={handlePrevNote}
                disabled={currentNoteIndex === 0}
              >
                ◀ Trang trước
              </button>
              <span className="small text-muted">
                Trang {currentNoteIndex + 1}/{revisionNotes.length}
              </span>
              <button
                type="button"
                className="btn btn-sm btn-outline-secondary"
                onClick={handleNextNote}
                disabled={currentNoteIndex === revisionNotes.length - 1}
              >
                Trang sau ▶
              </button>
            </div>
          </div>
          <div className="card-body">
            <CanvasMarkupTool
              key={currentNote.id}
              pageId={`revision-${currentNote.id}`}
              backgroundImageUrl={resolveImageUrl(
                currentNote.imageUrl,
                "https://placehold.co/800x1200?text=Anh+loi",
              )}
              loadDrawing={async () => null}
              readOnly={true}
              hideControls={true}
            />
          </div>
        </div>
      )}
      {canSubmit ? (
        <form onSubmit={handleSubmit} className="card shadow-sm">
          <div className="card-body">
            <label className="form-label fw-bold">
              Link file gốc chapter (Google Drive, OneDrive,...)
            </label>
            <input
              type="url"
              className="form-control mb-3"
              placeholder="https://..."
              value={manuscriptUrl}
              onChange={(e) => setManuscriptUrl(e.target.value)}
              disabled={submitting}
              required
            />
            <small className="text-muted d-block mb-3">
              Đây là thư mục/file lưu bản gốc chất lượng cao để Biên tập tải về
              kiểm tra, tách biệt với các ảnh preview đã upload khi tạo chapter.
            </small>
            {error && <div className="alert alert-danger py-2">{error}</div>}
            <button
              type="submit"
              className="btn btn-success"
              disabled={submitting}
            >
              {submitting
                ? "Đang gửi..."
                : isResubmit
                  ? "📤 Gửi lại cho Biên tập"
                  : "📤 Gửi cho Biên tập"}
            </button>
          </div>
        </form>
      ) : (
        <div className="alert alert-info">
          Chapter đang ở trạng thái <strong>{chapter?.status}</strong>, chưa
          cần/không thể gửi lại lúc này.
        </div>
      )}
    </div>
  );
}