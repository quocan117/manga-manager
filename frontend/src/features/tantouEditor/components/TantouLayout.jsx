import React from "react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import { getNotifications } from "../../../services/tantouService";
import useUnreadNotifications from "../../../hooks/useUnreadNotifications";
import "../styles/TantouEditor.css";

export default function TantouLayout() {
  const { user, logout } = useAuth();
  const { unreadCount, refresh: loadUnread } =
    useUnreadNotifications(getNotifications);

  const handleLogout = () => {
    if (window.confirm("Bạn có chắc chắn muốn đăng xuất?")) {
      logout();
    }
  };

  return (
    <div className="tantou-layout-container">
      <aside className="tantou-sidebar">
        <div className="sidebar-header">
          <h2 className="role-title">
            <span className="role-highlight">TANTOU</span>
            <br />
            EDITOR
          </h2>
          {user && (
            <p className="text-white-50 mb-0">Xin chào, {user.username}</p>
          )}
        </div>
        <ul className="sidebar-menu">
          <li>
            <NavLink
              to="/tantou"
              end
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <i className="fas fa-home"></i> Tổng Quan
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/tantou/notifications"
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <i className="fas fa-bell"></i> Thông Báo
              {unreadCount > 0 && (
                <span className="badge bg-danger ms-2">{unreadCount}</span>
              )}
            </NavLink>
          </li>
        </ul>
        <div className="sidebar-footer">
          <button className="btn-logout" onClick={handleLogout}>
            <i className="fas fa-sign-out-alt"></i> Đăng xuất
          </button>
        </div>
      </aside>
      <main className="tantou-main-content">
        <Outlet context={{ refreshUnreadCount: loadUnread }} />
      </main>
    </div>
  );
}
