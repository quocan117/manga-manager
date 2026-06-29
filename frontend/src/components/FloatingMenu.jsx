import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../styles/FloatingMenu.css";

const categories = [
  "Hành động", "Phiêu lưu", "Hài hước", "Tình cảm", "Siêu nhiên", 
  "Kinh dị", "Học đường", "Lịch sử", "Âm nhạc", "Phép thuật", 
  "Thể thao", "Đời thường", "Huyền bí", "Võ thuật", "Trinh thám"
];

const FloatingMenu = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [showGenres, setShowGenres] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const navigate = useNavigate();

  const handleNavigate = (path) => {
    navigate(path);
    resetMenu();
    window.scrollTo(0, 0);
  };
  const resetMenu = () => {
    setIsOpen(false);
    setShowGenres(false);
    setShowSearch(false);
    setSearchTerm("");
  };
  const handleSearchEnter = (e) => {
    if (e.key === "Enter" && searchTerm.trim() !== "") {
      handleNavigate(`/?search=${encodeURIComponent(searchTerm.trim())}`);
    }
  };
  const handleToggleMenu = () => {
    if (isOpen) resetMenu();
    else setIsOpen(true);
  };

  return (
    <div className="floating-container">
      {isOpen && (
        <div className="floating-menu">
          <button onClick={() => handleNavigate("/")}>🏠 Trang chủ</button>
          <button onClick={() => handleNavigate("/ranking")}>
            🏆 Xếp hạng
          </button>
          <button
            onClick={() => {
              setShowSearch(!showSearch);
              setShowGenres(false);
            }}
          >
            🔍 Tìm kiếm {showSearch ? "▾" : "▸"}
          </button>
          {showSearch && (
            <div className="floating-sub-panel">
              <input
                type="text"
                placeholder="Nhập từ khóa"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyDown={handleSearchEnter}
                autoFocus
              />
            </div>
          )}
          <div className="divider"></div>
          <button
            onClick={() => {
              setShowGenres(!showGenres);
              setShowSearch(false);
            }}
          >
            🏷️ Thể loại {showGenres ? "▾" : "▸"}
          </button>
          {showGenres && (
            <div className="floating-genres-grid">
              {categories.map((cat, idx) => (
                <span
                  key={idx}
                  onClick={() =>
                    handleNavigate(`/?genre=${encodeURIComponent(cat)}`)
                  }
                >
                  {cat}
                </span>
              ))}
            </div>
          )}
        </div>
      )}
      <button
        className={`floating-toggle ${isOpen ? "open" : ""}`}
        onClick={handleToggleMenu}
      >
        {isOpen ? "✕" : "☰"}
      </button>
    </div>
  );
};

export default FloatingMenu;
