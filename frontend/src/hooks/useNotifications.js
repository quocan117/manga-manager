import { useCallback, useEffect, useState } from "react";

export default function useNotifications(service) {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchNotifications = useCallback(async () => {
    try {
      setLoading(true);
      const data = await service.getNotifications();
      setNotifications(data || []);
    } catch (error) {
      console.error("Lỗi khi tải thông báo:", error);
    } finally {
      setLoading(false);
    }
  }, [service]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const markAsRead = useCallback(
    async (id) => {
      try {
        await service.markNotificationRead(id);
        setNotifications((prev) =>
          prev.map((n) => (n.id === id ? { ...n, isRead: true } : n)),
        );
        return true;
      } catch (error) {
        console.error("Lỗi khi đánh dấu đã đọc:", error);
        return false;
      }
    },
    [service],
  );

  return { notifications, loading, fetchNotifications, markAsRead };
}
