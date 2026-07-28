import React, { useState } from "react";
import "../styles/SeriesModal.css";

export default function RejectReasonModal({
  seriesTitle,
  submitting,
  onCancel,
  onConfirm,
  title = "Từ chối hồ sơ series",
  description = "Lý do này sẽ được lưu lại và hiển thị cho Hội đồng Biên tập nếu tất cả biên tập viên đều từ chối.",
  confirmLabel = "Xác nhận từ chối",
  submittingLabel = "Đang gửi...",
  confirmButtonClass = "btn btn-danger",
  requireReason = true,
  errorMessage = "Vui lòng nhập lý do từ chối trước khi tiếp tục.",
}) {
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  const handleConfirm = () => {
    const trimmed = reason.trim();
    if (requireReason && !trimmed) {
      setError(errorMessage);
      return;
    }
    setError("");
    onConfirm(trimmed);
  };

  return (
    <div className="custom-modal-overlay" onClick={onCancel}>
      <div
        className="custom-modal-content"
        onClick={(e) => e.stopPropagation()}
      >
        <button className="close-btn" onClick={onCancel} aria-label="Đóng">
          ✕
        </button>
        <h4 className="mb-2">{title}</h4>
        {seriesTitle && <p className="text-muted mb-3">{seriesTitle}</p>}
        <p className="mb-2">{description}</p>
        <textarea
          className="form-control mb-2"
          rows="4"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          placeholder={requireReason ? "" : "Không bắt buộc"}
          autoFocus
        />
        {error && <div className="text-danger mb-2">{error}</div>}
        <div className="d-flex gap-2 justify-content-end">
          <button
            className="btn btn-secondary"
            onClick={onCancel}
            disabled={submitting}
          >
            Hủy
          </button>
          <button
            className={confirmButtonClass}
            onClick={handleConfirm}
            disabled={submitting}
          >
            {submitting ? submittingLabel : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
