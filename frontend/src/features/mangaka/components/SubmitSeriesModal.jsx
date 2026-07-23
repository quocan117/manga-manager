import { useState, useEffect } from "react";
import {
  submitSeriesReview,
  getSeriesFiles,
} from "../../../services/mangakaService";
import "../../../styles/SeriesModal.css";
import "../styles/SubmitSeriesModal.css";

const ACCEPTED_EXTENSIONS =
  ".jpg,.jpeg,.png,.webp,.gif,.pdf,.txt,.md,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.zip";

export default function SubmitSeriesModal({ series, onClose, onSubmitted }) {
  const [files, setFiles] = useState([]);
  const [existingFiles, setExistingFiles] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const isResubmit = series?.status === "REVISION_REQUESTED";

  useEffect(() => {
    if (series?.id) {
      getSeriesFiles(series.id)
        .then(setExistingFiles)
        .catch(() => {});
    }
  }, [series?.id]);

  const handleFilePick = (e) => {
    const picked = Array.from(e.target.files || []);
    setFiles((prev) => [...prev, ...picked]);
    setError("");
  };
  const removeFile = (index) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (files.length === 0) {
      setError(
        "Vui lòng đính kèm ít nhất 1 file hồ sơ (ảnh, PDF, tài liệu hoặc ZIP).",
      );
      return;
    }
    try {
      setSubmitting(true);
      const updated = await submitSeriesReview(series.id, files);
      onSubmitted?.(updated);
      onClose();
    } catch (err) {
      const backendMessage =
        err?.response?.data?.message || err?.response?.data?.error;
      setError(
        backendMessage ||
          "Không thể gửi hồ sơ series lúc này. Vui lòng thử lại sau.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (!series) return null;
  return (
    <div className="custom-modal-overlay" onClick={onClose}>
      <div
        className="custom-modal-content submit-series-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <button className="close-btn" onClick={onClose} aria-label="Đóng">
          ✕
        </button>
        <h3 className="submit-series-title">
          {isResubmit
            ? "Gửi lại hồ sơ cho Biên tập"
            : "Gửi hồ sơ Series cho Biên tập"}
        </h3>
        <p className="submit-series-subtitle">
          Series: <strong>{series.title}</strong>
        </p>

        {existingFiles.length > 0 && (
          <div className="submit-series-hint mb-2">
            Lần nộp trước có {existingFiles.length} file. Nộp lại sẽ thay thế
            bằng file mới bên dưới.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">
              File hồ sơ (ảnh, PDF, Word, TXT hoặc ZIP — tối đa 20MB/file)
            </label>
            <input
              type="file"
              multiple
              accept={ACCEPTED_EXTENSIONS}
              className="form-control"
              onChange={handleFilePick}
              disabled={submitting}
            />
            <small className="text-muted">
              Ảnh/PDF sẽ được xem trực tiếp trên hệ thống. File ZIP hoặc tài
              liệu không hỗ trợ xem trước sẽ có nút Tải xuống cho Biên tập.
            </small>
          </div>

          {files.length > 0 && (
            <ul className="submit-series-filelist">
              {files.map((f, i) => (
                <li key={i}>
                  <span>{f.name}</span>
                  <span className="text-muted small">
                    ({Math.round(f.size / 1024)} KB)
                  </span>
                  <button
                    type="button"
                    onClick={() => removeFile(i)}
                    disabled={submitting}
                  >
                    ✕
                  </button>
                </li>
              ))}
            </ul>
          )}

          {error && <div className="alert alert-danger py-2">{error}</div>}
          <div className="d-flex gap-2 justify-content-end mt-4">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={submitting}
            >
              Hủy
            </button>
            <button
              type="submit"
              className="btn btn-success"
              disabled={submitting}
            >
              {submitting
                ? "Đang gửi..."
                : isResubmit
                  ? "Gửi lại cho Biên tập"
                  : "Gửi cho Biên tập"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
