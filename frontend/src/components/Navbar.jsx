import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { categories } from '../data/mockData';
import "../styles/Navbar.css";

const Navbar = () => {
  const navigate = useNavigate();
  const [isCategoryOpen, setIsCategoryOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const toggleCategory = () => {
    setIsCategoryOpen(!isCategoryOpen);
  };

  const handleCategoryClick = (category) => {
    navigate(`/?genre=${encodeURIComponent(category)}`);
    setIsCategoryOpen(false);
  };

  const handleSearch = (e) => {
    if (e.key === 'Enter') {
      if (searchTerm.trim() !== '') {
        navigate(`/?search=${encodeURIComponent(searchTerm.trim())}`);
      } else {
        navigate('/');
      }
    }
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="nav-logo" onClick={() => navigate('/')}>
          Manga Studio
        </div>
        <div className="nav-menu">
          <div className="nav-dropdown">
            <span className="nav-link" onClick={toggleCategory}>
              Thể loại {isCategoryOpen ? '▴' : '▾'}
            </span>
            {isCategoryOpen && (
              <div className="dropdown-content">
                {categories.map((category, index) => (
                  <div key={index} className="dropdown-item" onClick={() => handleCategoryClick(category)}>
                    {category}
                  </div>
                ))}
              </div>
            )}
          </div>
          <span className="nav-link">Xếp hạng</span>
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
          <button className="signup-btn" onClick={() => navigate("/register")}>
            Đăng ký
          </button>
          <button className="login-btn" onClick={() => navigate("/login")}>
            Đăng nhập
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
