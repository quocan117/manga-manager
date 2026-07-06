import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/Navbar.css";
const categories = [
  "Hành động", "Phiêu lưu", "Hài hước", "Tình cảm", "Siêu nhiên",
  "Kinh dị", "Học đường", "Lịch sử", "Âm nhạc", "Phép thuật",
  "Thể thao", "Đời thường", "Huyền bí", "Võ thuật", "Trinh thám"
];
const Navbar = () => {
  const navigate = useNavigate();
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const toggleCategory = () => {
    setIsCategoryOpen(!isCategoryOpen);
  };
  const handleCategoryClick = (category) => {
    navigate(`/?genre=${encodeURIComponent(category)}`);
    setIsCategoryOpen(false);
  };
  const handleSearch = (e) => {
    if (e.key === "Enter") {
      if (searchTerm.trim() !== "") {
        navigate(`/?search=${encodeURIComponent(searchTerm.trim())}`);
      } else {
        navigate("/");
      }
    }
  };
  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="nav-logo">
          Manga Studio
        </div>
        <div className="nav-menu">
          <span
            className="nav-link"
            onClick={() => {
              setSearchTerm(""); 
              navigate("/");
            }}
          >
            Trang chủ
          </span>
          <div className="nav-dropdown">
            <span className="nav-link" onClick={toggleCategory}>
              Thể loại {isCategoryOpen ? "▴" : "▾"}
            </span>
            {isCategoryOpen && (
              <div className="dropdown-content">
                {categories.map((category, index) => (
                  <div
                    key={index}
                    className="dropdown-item"
                    onClick={() => handleCategoryClick(category)}
                  >
                    {category}
                  </div>
                ))}
              </div>
            )}
          </div>
          <span className="nav-link" onClick={() => navigate("/ranking")}>
            Xếp hạng
          </span>
          <input
            className="search-input"
            type="text"
            placeholder="Tìm truyện, tác giả"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={handleSearch}
          />
        </div>
        <div className="auth-buttons">
          <button className="login-btn" onClick={() => navigate("/login")}>
            Đăng nhập
          </button>
        </div>
      </div>
    </nav>
  );
};
export default Navbar;