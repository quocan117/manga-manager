import React from "react";
import { Outlet, NavLink, useNavigate } from "react-router-dom";
import "../styles/EditorialBoard.css";

const BoardLayout = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/");
  };

  return (
    <div className="editorial-dashboard-container">
      <div className="sidebar">
        <h2 className="sidebar-title">EDITORIAL BOARD</h2>
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
