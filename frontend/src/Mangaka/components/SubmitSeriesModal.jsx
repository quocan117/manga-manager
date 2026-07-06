import { useState } from "react";
import { submitSeriesReview } from "../../services/mangakaService";
import "../../styles/SeriesModal.css";
import "../styles/SubmitSeriesModal.css";
export default function SubmitSeriesModal({ series, onClose, onSubmitted }) {
  const [storyboardUrl, setStoryboardUrl] = useState(series?.storyboardUrl || "");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const isResubmit = series?.status === "REVISION_REQUESTED";
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!storyboardUrl.trim()) {
      setError("Vui lòng nhập link bản thảo (storyboard) trước khi gửi.");
      return;
    }
    try {
      setSubmitting(true);
      const updated = await submitSeriesReview(series.id, storyboardUrl.trim());
      onSubmitted?.(updated);
      onClose();
    } catch (err) {
      console.error(err);
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
          {isResubmit ? "Gửi lại hồ sơ cho Biên tập" : "Gửi hồ sơ Series cho Biên tập"}
        </h3>
        <p className="submit-series-subtitle">
          Series: <strong>{series.title}</strong>
        </p>
        {isResubmit && (
          <div className="submit-series-hint warning">
            Series này trước đó đã bị yêu cầu chỉnh sửa. Hãy cập nhật lại link
            bản thảo rồi gửi lại cho biên tập phụ trách.
          </div>
        )}
        {!isResubmit && (
          <div className="submit-series-hint">
            Sau khi gửi, hệ thống sẽ tự động chọn biên tập viên đang có{" "}
            <strong>ít series đang xử lý nhất</strong> để tiếp nhận và kiểm
            tra hồ sơ của bạn trước khi trình lên Hội đồng Biên tập xét duyệt.
          </div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Link bản thảo (storyboard)</label>
            <input
              type="text"
              className="form-control"
              placeholder="https://..."
              value={storyboardUrl}
              onChange={(e) => setStoryboardUrl(e.target.value)}
              disabled={submitting}
              required
            />
            <small className="text-muted">
              Đường dẫn tới file/kịch bản bản thảo để biên tập tham khảo khi
              kiểm duyệt.
            </small>
          </div>
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
            <button type="submit" className="btn btn-success" disabled={submitting}>
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