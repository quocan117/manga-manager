import { useEffect, useState, useCallback } from "react";
import { Outlet, NavLink } from "react-router-dom";
import { getNotifications } from "../../services/assistantService";
import { useAuth } from "../../context/AuthContext";
export default function AssistantLayout() {
  const { user, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const loadUnread = useCallback(async () => {
    try {
      const data = await getNotifications();
      setUnreadCount(data.filter((n) => !n.isRead).length);
    } catch (error) {
      console.error(error);
    }
  }, []);
  useEffect(() => {
    loadUnread();
  }, [loadUnread]);
  const handleLogout = () => {
    logout();
  };
  return (
    <div className="container-fluid">
      <div className="row">
        <div className="col-md-2 sidebar">
          <div className="sidebar-title">
            ASSISTANT
          </div>
          <ul className="nav flex-column mt-4">
            <li>
              <NavLink to="/assistant" end className="sidebar-link">
                📊 Dashboard
              </NavLink>
            </li>
            <li>
              <NavLink to="/assistant/tasks" className="sidebar-link">
                📋 My Tasks
              </NavLink>
            </li>
            <li>
              <NavLink to="/assistant/submissions" className="sidebar-link">
                📤 My Submissions
              </NavLink>
            </li>
            <li>
              <NavLink to="/assistant/notifications" className="sidebar-link">
                🔔 Notifications
                {unreadCount > 0 && (
                  <span className="badge bg-danger ms-2">{unreadCount}</span>
                )}
              </NavLink>
            </li>
            <li className="mt-auto">
              <button className="logout-btn" onClick={handleLogout}>
                🚪 Logout
              </button>
            </li>
          </ul>
        </div>
        <div className="col-md-10 p-4">
          <div className="d-flex justify-content-between">
            <div>
              <h4>Xin chào, {user?.username || "Assistant"}</h4>
              <small>Assistant</small>
            </div>
          </div>
          <hr />
          <Outlet context={{ refreshUnreadCount: loadUnread }} />
        </div>
      </div>
    </div>
  );
}