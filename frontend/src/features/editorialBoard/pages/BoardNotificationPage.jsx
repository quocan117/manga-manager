import React, { useState, useEffect } from "react";
import { useNavigate, useOutletContext } from "react-router-dom";
import {
  getNotifications,
  markNotificationRead,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";

const NOTIFICATION_LABELS = {
  NEW_PUBLISH_SCHEDULE: "📅 Lịch xuất bản mới",
  SERIES_APPROVED: "✅ Series đã duyệt",
  SERIES_REJECTED: "❌ Series bị từ chối",
};

const NOTIFICATION_LINKS = {
  NEW_PUBLISH_SCHEDULE: "/board/schedule",
};

export default function Notification() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState("");
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
      setErrorMsg("Không thể tải danh sách thông báo. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const handleRead = async (notification) => {
    if (!notification.isRead) {
      try {
        await markNotificationRead(notification.id);
        setNotifications((prev) =>
          prev.map((n) =>
            n.id === notification.id ? { ...n, isRead: true } : n,
          ),
        );
        refreshUnreadCount?.();
      } catch (error) {
        console.error("Lỗi đánh dấu đã đọc:", error);
      }
    }
    const link = NOTIFICATION_LINKS[notification.type];
    if (link) {
      navigate(link);
    }
  };

  if (loading) return <div className="p-4">Đang tải thông báo...</div>;
  
  return (
    <div className="tab-content">
      <h2>🔔 Thông Báo</h2>
      {errorMsg && (
        <div className="alert alert-danger py-2" role="alert">
          {errorMsg}
        </div>
      )}
      {notifications.length === 0 && (
        <p className="text-muted">Chưa có thông báo nào.</p>
      )}
      {notifications.map((n) => {
        const label = NOTIFICATION_LABELS[n.type] || n.type;
        const clickable = !n.isRead || Boolean(NOTIFICATION_LINKS[n.type]);
        return (
          <div
            key={n.id}
            onClick={() => clickable && handleRead(n)}
            className={`card mb-2 border-0 shadow-sm ${n.isRead ? "" : "bg-primary bg-opacity-10"}`}
            style={{ cursor: clickable ? "pointer" : "default" }}
          >
            <div className="card-body py-2 d-flex justify-content-between align-items-center flex-wrap gap-2">
              <div>
                <span className="badge bg-secondary me-2">{label}</span>
                {n.message}
              </div>
              <small className="text-muted">
                {formatDateTime(n.createdAt)}
              </small>
            </div>
          </div>
        );
      })}
    </div>
  );
}