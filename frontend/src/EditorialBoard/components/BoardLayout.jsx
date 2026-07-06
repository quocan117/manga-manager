import React from "react";
import { Outlet, NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import "../styles/EditorialBoard.css";
const BoardLayout = () => {
  const { user, logout } = useAuth();
  const handleLogout = () => {
    logout();
  };
  return (
    <div className="editorial-dashboard-container">
      <div className="sidebar">
        <h2 className="sidebar-title">EDITORIAL BOARD</h2>
        {user && <p className="text-white-50 px-3 mb-0">Xin chào, {user.username}</p>}
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
              to="/board/manage-users"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              👤 Quản lý Tài Khoản
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
        <Outlet />
      </div>
    </div>
  );
};
export default BoardLayout;