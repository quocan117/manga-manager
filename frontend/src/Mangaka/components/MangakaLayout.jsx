import { useCallback, useEffect, useState } from "react";
import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { getNotifications } from "../../services/mangakaService";
import "../styles/MangakaLayout.css";

export default function MangakaLayout() {
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  let user = null;

  try {
    user = JSON.parse(localStorage.getItem("user"));
  } catch (error) {
    console.error("User parse error:", error);
    user = null;
  }

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
    localStorage.removeItem("token");
    localStorage.removeItem("user");

    navigate("/");
  };

  return (
    <div className="mangaka-layout">
      <div className="sidebar">
        <h3 className="sidebar-title">MANGAKA</h3>
        <ul className="nav flex-column mt-4">
          <li>
            <NavLink
              to="/mangaka"
              end
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active-link" : ""}`
              }
            >
              📊 Dashboard
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/mangaka/manga"
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active-link" : ""}`
              }
            >
              📚 My Series
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/mangaka/tasks"
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active-link" : ""}`
              }
            >
              🎨 Assistant Tasks
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/mangaka/ranking"
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active-link" : ""}`
              }
            >
              🏆 Ranking
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/mangaka/notifications"
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active-link" : ""}`
              }
            >
              🔔 Notifications
              {unreadCount > 0 && ( 
                <span className="badge bg-danger ms-2">{unreadCount}</span>
              )}
            </NavLink>
          </li>

          <li>
            <NavLink
              to="/mangaka/manage-assistants"
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active-link" : ""}`
              }
            >
              👥 Quản lý Trợ lý
            </NavLink>
          </li>

          <li className="mt-4">
            <button className="logout-btn" onClick={handleLogout}>
              🚪 Logout
            </button>
          </li>
        </ul>
      </div>

      <div className="mangaka-content p-4">
        <div className="card shadow-sm border-0 mb-4">
          <div className="card-body d-flex justify-content-between align-items-center">
            <div>
              <h4 className="mb-0">
                {user
                  ? `Xin chào, ${user.username}`
                  : "Chưa có thông tin tài khoản"}
              </h4>

              <small className="text-muted">{user?.role || "Guest"}</small>
            </div>

            <div className="text-end">
              <span className="badge bg-success">Online</span>

              <br />

              <small className="text-muted">
                {user?.role || "ROLE_UNKNOWN"}
              </small>
            </div>
          </div>
        </div>

        <Outlet context={{ refreshUnreadCount: loadUnread }} />
      </div>
    </div>
  );
}
