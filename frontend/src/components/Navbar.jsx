import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Navbar.css";

const Navbar = () => {
  const navigate = useNavigate();

  return (
    <nav className="navbar">
      <div className="nav-logo" onClick={() => navigate("/")}>
        Manga Studio
      </div>
      <div className="nav-menu">
        <span className="nav-link">Thể loại</span>
        <span className="nav-link">Xếp hạng</span>
        <input
          className="search-input"
          type="text"
          placeholder="Tìm truyện, tác giả..."
        />
      </div>
      <div className="auth-buttons">
        <button className="signup-btn" onClick={() => navigate("/register")}>
          Đăng ký
        </button>
        <button className="login-btn" onClick={() => navigate("/login")}>
          Đăng nhập
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
