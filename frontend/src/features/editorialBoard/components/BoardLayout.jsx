import React from "react";
import { Outlet, NavLink } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import { getNotifications } from "../../../services/boardService";
import useUnreadNotifications from "../../../hooks/useUnreadNotifications";
import "../styles/EditorialBoard.css";

const BoardLayout = () => {
  const { user, logout } = useAuth();
  const { unreadCount, refresh: loadUnread } =
    useUnreadNotifications(getNotifications);

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="editorial-dashboard-container">
      <div className="sidebar">
        <h2 className="sidebar-title">EDITORIAL BOARD</h2>
        {user && (
          <p className="text-white-50 px-3 mb-0">Xin chào, {user.username}</p>
        )}
        <ul className="sidebar-menu">
          <li>
            <NavLink
              to="/board/ranking"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              📊 Bảng Xếp Hạng
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/board/review"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              📝 Xét Duyệt Tác Phẩm
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/board/schedule"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              📅 Lịch Xuất Bản
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/board/reader-votes"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              🗳️ Bình Chọn Độc Giả
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/board/manage-users"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              👤 Quản lý Tài Khoản
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/board/notifications"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              🔔 Thông Báo
              {unreadCount > 0 && (
                <span className="badge bg-danger ms-2">{unreadCount}</span>
              )}
            </NavLink>
          </li>
          <li style={{ marginTop: "auto", padding: "20px" }}>
            <button className="btn btn-danger w-100" onClick={handleLogout}>
              Đăng xuất
            </button>
          </li>
        </ul>
      </div>
      <div className="main-board-content">
        <Outlet context={{ refreshUnreadCount: loadUnread }} />
      </div>
    </div>
  );
};
export default BoardLayout;
