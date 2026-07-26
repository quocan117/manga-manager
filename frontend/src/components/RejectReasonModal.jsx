import React, { useState } from "react";
import "../styles/SeriesModal.css";

export default function RejectReasonModal({
  seriesTitle,
  submitting,
  onCancel,
  onConfirm,
}) {
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  const handleConfirm = () => {
    const trimmed = reason.trim();
    if (!trimmed) {
      setError("Vui lòng nhập lý do từ chối trước khi tiếp tục.");
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
        <h4 className="mb-2">Từ chối hồ sơ series</h4>
        {seriesTitle && <p className="text-muted mb-3">{seriesTitle}</p>}
        <p className="mb-2">
          Lý do này sẽ được lưu lại và hiển thị cho Hội đồng Biên tập nếu tất cả
          biên tập viên đều từ chối.
        </p>
        <textarea
          className="form-control mb-2"
          rows="4"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
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
            className="btn btn-danger"
            onClick={handleConfirm}
            disabled={submitting}
          >
            {submitting ? "Đang gửi..." : "Xác nhận từ chối"}
          </button>
        </div>
      </div>
    </div>
  );
}
