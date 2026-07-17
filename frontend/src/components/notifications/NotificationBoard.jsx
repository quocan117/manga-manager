import { useOutletContext } from "react-router-dom";
import useNotifications from "../../hooks/useNotifications";
import { formatDateTime } from "../../utils/formatDate";

export default function NotificationBoard({
  title,
  service,
  getBadgeClass = () => "bg-primary",
}) {
  const { notifications, loading, markAsRead } = useNotifications(service);
  const { refreshUnreadCount } = useOutletContext() || {};

  const handleClick = async (notification) => {
    if (notification.isRead) return;
    const success = await markAsRead(notification.id);
    if (success) refreshUnreadCount?.();
  };

  if (loading) return <div className="text-center mt-4">Loading...</div>;

  return (
    <div className="card shadow border-0">
      <div className="card-header bg-white d-flex justify-content-between align-items-center">
        <h5 className="mb-0">{title}</h5>
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
              <span className={`badge ${getBadgeClass(notification.type)}`}>
                {notification.type}
              </span>
              <small className="text-muted">
                {formatDateTime(notification.createdAt)}
              </small>
            </div>
            <p className="mt-2 mb-0">{notification.message}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
