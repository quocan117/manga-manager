import React from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import "../styles/TantouEditor.css";

export default function TantouLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm("Bạn có chắc chắn muốn đăng xuất?")) {
      localStorage.removeItem("token");
      localStorage.removeItem("userRole");
      navigate("/login");
    }
  };

  return (
    <div className="dashboard-layout">
      <aside className="tantou-sidebar">
        <div className="sidebar-header">
          <h2 className="role-title">
            <span className="role-highlight">TANTOU</span>
            <br />
            EDITOR
          </h2>
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
              to="/tantou/schedule"
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              <i className="fas fa-calendar-alt"></i> Lịch Xuất Bản
            </NavLink>
          </li>
        </ul>

        <div className="sidebar-footer">
          <button className="btn-logout" onClick={handleLogout}>
            <i className="fas fa-sign-out-alt"></i> Đăng xuất
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
