import { useEffect, useState } from "react";
import {
  getNotifications,
  markNotificationRead,
} from "../../services/assistantService";

export default function Notification() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchNotifications();
  }, []);

  const fetchNotifications = async () => {
    try {
      const data = await getNotifications();
      setNotifications(data);
    } catch (error) {
      console.error("Lỗi khi tải thông báo:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleClick = async (notification) => {
    if (notification.isRead) return;
    try {
      await markNotificationRead(notification.id);
      setNotifications((prev) =>
        prev.map((n) =>
          n.id === notification.id ? { ...n, isRead: true } : n,
        ),
      );
    } catch (error) {
      console.error("Lỗi khi đánh dấu đã đọc:", error);
    }
  };

  if (loading) return <div className="text-center mt-4">Loading...</div>;

  return (
    <div className="card shadow border-0">
      <div className="card-header bg-white d-flex justify-content-between align-items-center">
        <h5 className="mb-0">Thông báo</h5>
        <span className="badge bg-danger">
          {notifications.filter((n) => !n.isRead).length} chưa đọc
        </span>
      </div>

      <div className="card-body">
        {notifications.length === 0 && (
          <div className="text-center text-muted">Không có thông báo nào</div>
        )}

        {notifications.map((notification) => (
          <div
            key={notification.id}
            role="button"
            onClick={() => handleClick(notification)}
            className={`border rounded p-3 mb-3 ${
              notification.isRead ? "bg-light" : "bg-warning-subtle"
            }`}
          >
            <div className="d-flex justify-content-between">
              <span className="badge bg-primary">{notification.type}</span>
              <small className="text-muted">
                {new Date(notification.createdAt).toLocaleString()}
              </small>
            </div>
            <p className="mt-2 mb-0">{notification.message}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
