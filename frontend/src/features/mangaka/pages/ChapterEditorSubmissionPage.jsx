import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getChapterById } from "../../../services/mangakaService";
import {
  submitChapterToEditor,
  getChapterRevisionNotes,
} from "../../../services/chapterEditorService";
import { resolveImageUrl } from "../../../utils/imageUrl";
import CanvasMarkupTool from "../../../components/CanvasMarkupTool";
import SeriesFileList from "../../../components/SeriesFileList";

const SUBMITTABLE_STATUSES = ["DRAFT", "REVISION_REQUESTED", undefined, null];
const IMAGE_EXTENSIONS = [".jpg", ".jpeg", ".png", ".webp", ".gif"];

function getExtension(name = "") {
  const idx = name.lastIndexOf(".");
  return idx >= 0 ? name.slice(idx).toLowerCase() : "";
}
function validateManuscriptFiles(files) {
  if (!files.length) return "Vui lòng chọn ít nhất 1 ảnh hoặc 1 file ZIP.";
  const extensions = files.map((f) => getExtension(f.name));
  const hasZip = extensions.includes(".zip");
  if (hasZip) {
    if (files.length !== 1)
      return "Nếu chọn ZIP thì chỉ được gửi đúng 1 file ZIP duy nhất.";
  } else if (extensions.some((ext) => !IMAGE_EXTENSIONS.includes(ext))) {
    return "Chỉ chấp nhận ảnh (JPG, PNG, WEBP, GIF) hoặc 1 file ZIP duy nhất.";
  }
  return "";
}

export default function ChapterEditorSubmission() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState(null);
  const [selectedFiles, setSelectedFiles] = useState([]);
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

  const handleAddFiles = (e) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    const newExtensions = files.map((f) => getExtension(f.name));
    const newHasZip = newExtensions.includes(".zip");

    if (newHasZip) {
      if (files.length !== 1) {
        alert(
          "Mỗi lần chỉ được chọn đúng 1 file ZIP, không chọn kèm file khác.",
        );
        e.target.value = "";
        return;
      }
      setSelectedFiles((prev) => {
        if (prev.length > 0) {
          alert(
            "ZIP chứa toàn bộ chapter nên không thể gửi kèm ảnh lẻ. " +
            "Danh sách ảnh đã chọn trước đó sẽ được thay bằng file ZIP này.",
          );
        }
        return files;
      });
    } else {
      setSelectedFiles((prev) => {
        const prevHasZip = prev.some((f) => getExtension(f.name) === ".zip");
        if (prevHasZip) {
          alert(
            "Bạn đang chọn ảnh lẻ nên file ZIP đã chọn trước đó sẽ bị bỏ, " +
            "chỉ giữ lại các ảnh.",
          );
          return files;
        }
        return [...prev, ...files];
      });
    }

    e.target.value = "";
  };

  const handleRemoveFile = (index) => {
    setSelectedFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleClearFiles = () => setSelectedFiles([]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    const validationError = validateManuscriptFiles(selectedFiles);
    if (validationError) {
      setError(validationError);
      return;
    }
    try {
      setSubmitting(true);
      const updated = await submitChapterToEditor(chapterId, selectedFiles);
      setChapter((prev) => ({ ...prev, ...updated }));
      setSelectedFiles([]);
      alert("Đã gửi hồ sơ chapter cho Biên tập!");
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
          Gửi hồ sơ Chapter {chapter?.chapterNumber ?? "?"} cho Biên tập
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
          bên dưới, chỉnh sửa lại nội dung, rồi gửi lại hồ sơ cho biên tập.
        </div>
      )}

      <div className="card shadow-sm mb-4">
        <div className="card-header bg-white fw-bold">
          File hồ sơ đã gửi cho Biên tập
        </div>
        <div className="card-body">
          <SeriesFileList
            files={chapter?.manuscriptFiles || []}
            emptyText="Chưa gửi file nào."
          />
        </div>
      </div>

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
            {currentNote.description && (
              <div className="alert alert-warning">
                <strong>Mô tả lỗi từ Biên tập:</strong> {currentNote.description}
              </div>
            )}
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
              Ảnh trang truyện (chọn nhiều) hoặc 1 file ZIP chứa toàn bộ chapter
            </label>
            <input
              type="file"
              className="form-control mb-2"
              multiple
              accept="image/png,image/jpeg,image/webp,image/gif,.zip"
              onChange={handleAddFiles}
              disabled={submitting}
            />

            {selectedFiles.length > 0 && (
              <div className="mb-3">
                <div className="d-flex justify-content-between align-items-center mb-1">
                  <small className="text-muted">
                    Đã chọn {selectedFiles.length} file:
                  </small>
                  <button
                    type="button"
                    className="btn btn-sm btn-link text-danger p-0"
                    onClick={handleClearFiles}
                    disabled={submitting}
                  >
                    Xoá tất cả
                  </button>
                </div>
                <ul className="list-group">
                  {selectedFiles.map((file, idx) => (
                    <li
                      key={`${file.name}-${idx}`}
                      className="list-group-item d-flex justify-content-between align-items-center py-1"
                    >
                      <span className="text-truncate">{file.name}</span>
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => handleRemoveFile(idx)}
                        disabled={submitting}
                      >
                        ✕
                      </button>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <small className="text-muted d-block mb-3">
              Có thể bấm chọn file nhiều lần để gộp thêm ảnh; nếu chọn ZIP,
              danh sách sẽ tự chuyển sang chỉ dùng ZIP (không trộn được với
              ảnh lẻ).
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