import { Outlet, NavLink } from "react-router-dom";
import { getNotifications } from "../../../services/assistantService";
import { useAuth } from "../../../context/AuthContext";
import useUnreadNotifications from "../../../hooks/useUnreadNotifications";

export default function AssistantLayout() {
  const { user, logout } = useAuth();
  const { unreadCount, refresh: loadUnread } =
    useUnreadNotifications(getNotifications);
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
                📊 Tổng quan
              </NavLink>
            </li>
            <li>
              <NavLink to="/assistant/tasks" className="sidebar-link">
                📋 Công việc của tôi
              </NavLink>
            </li>
            <li>
              <NavLink to="/assistant/submissions" className="sidebar-link">
                📤 Bài nộp 
              </NavLink>
            </li>
            <li>
              <NavLink to="/assistant/notifications" className="sidebar-link">
                🔔 Thông báo
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