import React, { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import SeriesCard from "../components/SeriesCard";
import SeriesModal from "../components/SeriesModal";
import FloatingMenu from "../components/FloatingMenu";
import { trendingSeries } from "../data/mockData";
import "../styles/HomePage.css";

const HomePage = () => {
  // 1. QUẢN LÝ STATE & ROUTING
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [selectedSeries, setSelectedSeries] = useState(null);
  const [jumpPage, setJumpPage] = useState("");
  // Trích xuất parameters từ URL
  const currentGenre = searchParams.get("genre");
  const searchQuery = searchParams.get("search");
  const isFiltering = currentGenre || searchQuery;
  const currentPage = parseInt(searchParams.get("page")) || 1;

  // 2. XỬ LÝ DỮ LIỆU
  // Lấy Top 4 Series có lượt Vote cao nhất
  const top4Series = [...trendingSeries]
    .sort((a, b) => {
      const totalA = a.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
      const totalB = b.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
      return totalB - totalA;
    })
    .slice(0, 4);

  // Xử lý Lọc & Tìm kiếm
  let filteredSeries = trendingSeries;
  let filterTitle = "";
  
  if (currentGenre) {
    filteredSeries = trendingSeries.filter((series) =>
      series.genres?.includes(currentGenre),
    );
    filterTitle = `🏷️ Thể loại: ${currentGenre} (${filteredSeries.length} series)`;
  } else if (searchQuery) {
    const query = searchQuery.toLowerCase();
    filteredSeries = trendingSeries.filter(
      (series) =>
        series.title.toLowerCase().includes(query) ||
        series.author.toLowerCase().includes(query),
    );
    filterTitle = `🔍 Kết quả tìm kiếm: "${searchQuery}" (${filteredSeries.length} series)`;
  }

  // Xử lý dữ liệu cho khu vực "Khám phá" (Loại trừ Top 4 và Phân trang)
  const ITEMS_PER_PAGE = 20;
  const top4Ids = top4Series.map((series) => series.id);
  // Lấy các truyện còn lại (trừ top 4) và xếp mới nhất lên đầu
  const remainingSeries = trendingSeries
    .filter((series) => !top4Ids.includes(series.id))
    .sort((a, b) => b.id - a.id);
  const totalPages = Math.ceil(remainingSeries.length / ITEMS_PER_PAGE) || 1;
  const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
  const seriesToShow = remainingSeries.slice(
    startIndex,
    startIndex + ITEMS_PER_PAGE,
  );

  // 3. CÁC HÀM XỬ LÝ SỰ KIỆN
  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      const newParams = new URLSearchParams(searchParams);
      newParams.set("page", newPage);
      navigate(`/?${newParams.toString()}`);
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  };

  const handleJumpPage = (e) => {
    e.preventDefault();
    const pageNum = parseInt(jumpPage);
    if (pageNum) {
      handlePageChange(pageNum);
      setJumpPage("");
    }
  };

  // 4. HIỂN THỊ GIAO DIỆN
  return (
    <div className="home-container">
      <Navbar />
      <div className="vote-banner">
        <h1 className="banner-title">ĐẠI CHIẾN MANGA: BẢNG XẾP HẠNG</h1>
        <p className="banner-subtitle">
          Mỗi lượt <span className="highlight-text">VOTE</span> là một lá phiếu
          định đoạt danh tiếng của Series. Series nào sẽ thống trị đỉnh bảng?
          Series nào sẽ bị lãng quên? Quyền năng đó nằm trong tay bạn. Hãy hành
          động ngay!
        </p>
      </div>

      <div className="main-content">
        {isFiltering ? (
          // Lọc và tìm kiếm
          <section>
            <h2 className="section-title">{filterTitle}</h2>
            <div className="cards-grid">
              {filteredSeries.length > 0 ? (
                filteredSeries.map((series) => (
                  <SeriesCard
                    key={series.id}
                    series={series}
                    onClick={setSelectedSeries}
                  />
                ))
              ) : (
                <div className="empty-state">
                  <h3 className="empty-title">
                    Không tìm thấy kết quả nào phù hợp 
                  </h3>
                  <p className="empty-subtitle">
                    Vui lòng thử lại với từ khóa hoặc thể loại khác.
                  </p>
                </div>
              )}
            </div>
          </section>
        ) : (
          // Trang chủ mặc định
          <>
            <section>
              <h2 className="section-title">Các Series Nổi Bật</h2>
              <div className="cards-grid">
                {top4Series.map((series) => (
                  <SeriesCard
                    key={series.id}
                    series={series}
                    onClick={setSelectedSeries}
                  />
                ))}
              </div>
            </section>

            <section style={{ marginTop: "50px" }}>
              <h2 className="section-title">Khám Phá Các Series Khác</h2>
              <div className="cards-grid">
                {seriesToShow.map((series) => (
                  <SeriesCard
                    key={series.id}
                    series={series}
                    onClick={setSelectedSeries}
                  />
                ))}
              </div>

              {totalPages > 1 && (
                <div className="pagination-container">
                  <div className="pagination-buttons">
                    <button
                      disabled={currentPage === 1}
                      onClick={() => handlePageChange(currentPage - 1)}
                    >
                      « Trước
                    </button>
                    <span className="page-info">
                      Trang <strong>{currentPage}</strong> / {totalPages}
                    </span>
                    <button
                      disabled={currentPage === totalPages}
                      onClick={() => handlePageChange(currentPage + 1)}
                    >
                      Sau »
                    </button>
                  </div>

                  <form className="jump-page-form" onSubmit={handleJumpPage}>
                    <input
                      type="number"
                      min="1"
                      max={totalPages}
                      placeholder="..."
                      value={jumpPage}
                      onChange={(e) => setJumpPage(e.target.value)}
                    />
                    <button type="submit">Tới trang</button>
                  </form>
                </div>
              )}
            </section>
          </>
        )}
      </div>

      {selectedSeries && (
        <SeriesModal
          series={selectedSeries}
          onClose={() => setSelectedSeries(null)}
        />
      )}
      <FloatingMenu />
    </div>
  );
};

export default HomePage;
