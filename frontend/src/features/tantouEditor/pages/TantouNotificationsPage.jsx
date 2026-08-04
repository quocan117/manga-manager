import React, { useState, useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import {
  getNotifications,
  markNotificationRead,
  acceptSeries,
  rejectSeries,
  getSeriesDossier,
} from "../../../services/tantouService";
import { formatDateTime } from "../../../utils/formatDate";
import RejectReasonModal from "../../../components/RejectReasonModal";
import SeriesFileList from "../../../components/SeriesFileList";

const ASSIGNMENT_NOTIFICATION_TYPES = [
  "NEW_ASSIGNMENT",
  "SYSTEM_ASSIGNMENT",
  "FORCED_EDITOR_ASSIGNMENT",
];
const LOCKED_ASSIGNMENT_TYPES = ["FORCED_EDITOR_ASSIGNMENT"];

export default function TantouNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [acceptingId, setAcceptingId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [errorMsg, setErrorMsg] = useState("");

  const [previewDossier, setPreviewDossier] = useState(null);
  const [previewTargetId, setPreviewTargetId] = useState(null);

  const navigate = useNavigate();
  const { refreshUnreadCount } = useOutletContext() || {};

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const data = await getNotifications();
      setNotifications(data || []);
    } catch (error) {
      console.error("Lỗi lấy thông báo:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleRead = async (id) => {
    try {
      await markNotificationRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)),
      );
      refreshUnreadCount?.();
    } catch (error) {
      console.error("Lỗi đánh dấu đã đọc:", error);
    }
  };

  const handleOpenPreview = async (notification, e) => {
    e.stopPropagation();
    try {
      setPreviewTargetId(notification.id);
      const data = await getSeriesDossier(notification.referenceId);
      setPreviewDossier({ dossier: data, notification });
    } catch (error) {
      console.error("Lỗi khi tải bản xem trước hồ sơ:", error);
      alert("Không thể tải bản xem trước hồ sơ lúc này. Vui lòng thử lại.");
    } finally {
      setPreviewTargetId(null);
    }
  };

  const handleAccept = async (notification) => {
    const seriesId = notification.referenceId;
    if (!seriesId) return;
    setErrorMsg("");
    setAcceptingId(notification.id);
    try {
      await acceptSeries(seriesId);
      if (!notification.isRead) {
        await markNotificationRead(notification.id);
      }
      setNotifications((prev) =>
        prev.map((n) =>
          n.id === notification.id ? { ...n, isRead: true, accepted: true } : n,
        ),
      );
      refreshUnreadCount?.();
      navigate(`/tantou/review/${seriesId}`);
    } catch (error) {
      console.error("Lỗi nhận hồ sơ:", error);
      const message =
        error?.response?.data?.message ||
        "Không thể nhận hồ sơ. Có thể hồ sơ đã bị thu hồi hoặc biên tập khác đã nhận trước.";
      setErrorMsg(message);
      fetchNotifications();
    } finally {
      setAcceptingId(null);
    }
  };

  const handleReject = (notification) => {
    if (!notification.referenceId) return;
    setRejectTarget(notification);
  };

  const handleConfirmReject = async (reason) => {
    const notification = rejectTarget;
    if (!notification) return;
    const seriesId = notification.referenceId;
    setErrorMsg("");
    setRejectingId(notification.id);
    try {
      await rejectSeries(seriesId, reason);
      if (!notification.isRead) {
        await markNotificationRead(notification.id);
      }
      setNotifications((prev) =>
        prev.map((n) =>
          n.id === notification.id ? { ...n, isRead: true, rejected: true } : n,
        ),
      );
      refreshUnreadCount?.();
      setRejectTarget(null);
    } catch (error) {
      console.error("Lỗi từ chối hồ sơ:", error);
      const message =
        error?.response?.data?.message ||
        "Không thể từ chối hồ sơ. Vui lòng thử lại.";
      setErrorMsg(message);
      fetchNotifications();
    } finally {
      setRejectingId(null);
    }
  };

  if (loading) return <div className="p-4">Đang tải thông báo...</div>;

  return (
    <div className="p-4 bg-light min-vh-100">
      <h2 className="mb-4">🔔 Thông Báo</h2>
      {errorMsg && (
        <div className="alert alert-danger py-2" role="alert">
          {errorMsg}
        </div>
      )}
      {notifications.length === 0 && (
        <p className="text-muted">Chưa có thông báo nào.</p>
      )}
      {notifications.map((n) => {
        const isAssignment = ASSIGNMENT_NOTIFICATION_TYPES.includes(n.type);
        const canAccept = isAssignment && !n.isRead && !n.accepted;

        return (
          <div
            key={n.id}
            onClick={() => !n.isRead && !isAssignment && handleRead(n.id)}
            className={`card mb-2 border-0 shadow-sm ${n.isRead ? "" : "bg-primary bg-opacity-10"}`}
            style={{ cursor: n.isRead ? "default" : "pointer" }}
          >
            <div className="card-body py-2 d-flex justify-content-between align-items-center flex-wrap gap-2">
              <div>
                <span className="badge bg-secondary me-2">{n.type}</span>
                {n.message}
              </div>
              <div className="d-flex align-items-center gap-2">
                {canAccept && (
                  <button
                    type="button"
                    className="btn btn-sm btn-info text-white fw-bold"
                    disabled={previewTargetId === n.id}
                    onClick={(e) => handleOpenPreview(n, e)}
                  >
                    {previewTargetId === n.id
                      ? "Đang tải hồ sơ..."
                      : "👁️ Xem hồ sơ"}
                  </button>
                )}
                <small className="text-muted">
                  {formatDateTime(n.createdAt)}
                </small>
              </div>
            </div>
          </div>
        );
      })}

      {previewDossier && (
        <div
          className="custom-modal-overlay"
          onClick={() => setPreviewDossier(null)}
        >
          <div
            className="custom-modal-content"
            style={{
              width: "850px",
              maxWidth: "95vw",
              maxHeight: "90vh",
              overflowY: "auto",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="close-btn"
              onClick={() => setPreviewDossier(null)}
            >
              ✕
            </button>
            <h4 className="mb-4">
              Xem trước hồ sơ: {previewDossier.dossier.series.title}
            </h4>
            <div className="row mb-4">
              <div className="col-md-4">
                <img
                  src={`http://localhost:8080/covers/${previewDossier.dossier.series.coverUrl}`}
                  alt={previewDossier.dossier.series.title}
                  className="img-fluid rounded shadow-sm"
                  onError={(e) =>
                    (e.target.src =
                      "https://placehold.co/250x350?text=No+Cover")
                  }
                />
              </div>
              <div className="col-md-8">
                <p>
                  <strong>👤 Tác giả:</strong>{" "}
                  {previewDossier.dossier.series.author}
                </p>
                <p>
                  <strong>🏷️ Thể loại:</strong>{" "}
                  {previewDossier.dossier.series.genres?.join(", ") || "N/A"}
                </p>
                <p>
                  <strong>📖 Mô tả:</strong>{" "}
                  {previewDossier.dossier.series.description ||
                    "Chưa có mô tả."}
                </p>
              </div>
            </div>

            <h6 className="fw-bold mb-3 border-bottom pb-2">
              Bản thảo & Tài liệu đính kèm
            </h6>
            <div className="mb-4 bg-light p-3 rounded border">
              <SeriesFileList
                files={previewDossier.dossier.series.uploadedFiles || []}
                emptyText="Mangaka chưa tải lên bản thảo nào."
              />
            </div>

            <div className="d-flex justify-content-end gap-3 border-top pt-3">
              <button
                className="btn btn-secondary"
                onClick={() => setPreviewDossier(null)}
              >
                Đóng
              </button>
              {!LOCKED_ASSIGNMENT_TYPES.includes(
                previewDossier.notification.type,
              ) && (
                <button
                  className="btn btn-outline-danger px-4"
                  disabled={acceptingId === previewDossier.notification.id}
                  onClick={() => {
                    handleReject(previewDossier.notification);
                    setPreviewDossier(null);
                  }}
                >
                  Từ chối
                </button>
              )}
              <button
                className="btn btn-success px-4"
                disabled={acceptingId === previewDossier.notification.id}
                onClick={() => {
                  handleAccept(previewDossier.notification);
                  setPreviewDossier(null);
                }}
              >
                {acceptingId === previewDossier.notification.id
                  ? "Đang xử lý..."
                  : "✅ Chấp nhận phụ trách"}
              </button>
            </div>
          </div>
        </div>
      )}

      {rejectTarget && (
        <RejectReasonModal
          seriesTitle={rejectTarget.message}
          submitting={rejectingId === rejectTarget.id}
          onCancel={() => setRejectTarget(null)}
          onConfirm={handleConfirmReject}
        />
      )}
    </div>
  );
}
