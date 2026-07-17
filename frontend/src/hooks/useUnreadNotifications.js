import { useCallback, useEffect, useState } from "react";

export default function useUnreadNotifications(getNotifications) {
  const [unreadCount, setUnreadCount] = useState(0);

  const refresh = useCallback(async () => {
    try {
      const data = await getNotifications();
      setUnreadCount(data.filter((n) => !n.isRead).length);
    } catch (error) {
      console.error(error);
    }
  }, [getNotifications]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { unreadCount, refresh };
}
