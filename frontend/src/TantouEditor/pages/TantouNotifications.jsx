import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  getNotifications,
  markNotificationRead,
  acceptSeries,
} from "../../services/tantouService";

const ASSIGNMENT_NOTIFICATION_TYPES = ["NEW_ASSIGNMENT", "SYSTEM_ASSIGNMENT"];

export default function TantouNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [acceptingId, setAcceptingId] = useState(null);
  const [errorMsg, setErrorMsg] = useState("");
  const navigate = useNavigate();

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
    } catch (error) {
      console.error("Lỗi đánh dấu đã đọc:", error);
    }
  };

  const handleAccept = async (notification, e) => {
    e.stopPropagation();
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
        const isAccepting = acceptingId === n.id;

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
                    className="btn btn-sm btn-success"
                    disabled={isAccepting}
                    onClick={(e) => handleAccept(n, e)}
                  >
                    {isAccepting ? "Đang nhận..." : "Nhận hồ sơ series"}
                  </button>
                )}
                <small className="text-muted">
                  {new Date(n.createdAt).toLocaleString()}
                </small>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
