import React from "react";
import { Outlet, NavLink } from "react-router-dom";
import "../styles/EditorialBoard.css";

const BoardLayout = () => {
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
              to="/board/review-series"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              📚 Duyệt Series Mới
            </NavLink>
          </li>
          <li>
            <NavLink
              to="/board/review-mangaka"
              className={({ isActive }) => (isActive ? "active" : "")}
            >
              👤 Duyệt Mangaka
            </NavLink>
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
