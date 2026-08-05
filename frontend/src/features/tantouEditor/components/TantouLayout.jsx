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
            <span>TANTOU EDITOR</span>
          </h2>
          {user && (
            <div className="mt-3">
              <p className="text-white-50 mb-2 fw-bold">
                Xin chào, {user.username}
              </p>
              {user.specialty && (
                <div
                  className="badge bg-secondary text-wrap text-start lh-base"
                  style={{ fontSize: "11px", padding: "8px" }}
                >
                  <span className="text-warning">★ Chuyên môn:</span>
                  <br />
                  {user.specialty}
                </div>
              )}
            </div>
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
              to="/tantou/pending-series"
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <i className="fas fa-file-signature"></i> Bản thảo cần duyệt
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/tantou/pending-chapters"
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <i className="fas fa-book-open"></i> Chapter cần duyệt
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
