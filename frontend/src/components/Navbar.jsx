import React from "react";
import { useNavigate } from "react-router-dom";
import "./Navbar.css"; 

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
      <button className="login-btn" onClick={() => navigate("/login")}>
        Đăng nhập hệ thống
      </button>
    </nav>
  );
};

export default Navbar;
