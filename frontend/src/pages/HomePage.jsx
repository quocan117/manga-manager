import React, { useState } from "react";
import { useSearchParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import SeriesCard from "../components/SeriesCard";
import SeriesModal from "../components/SeriesModal";
import { trendingSeries } from "../data/mockData";
import "../styles/HomePage.css";

const HomePage = () => {
  const [selectedSeries, setSelectedSeries] = useState(null);
  const [searchParams] = useSearchParams();
  const currentGenre = searchParams.get("genre");
  const searchQuery = searchParams.get("search");

  let displaySeries = trendingSeries;
  if (currentGenre) {
    displaySeries = displaySeries.filter((series) =>
      series.genres?.includes(currentGenre),
    );
  }
  if (searchQuery) {
    const lowerCaseQuery = searchQuery.toLowerCase();
    displaySeries = displaySeries.filter(
      (series) =>
        series.title.toLowerCase().includes(lowerCaseQuery) ||
        series.author.toLowerCase().includes(lowerCaseQuery),
    );
  }
  let sectionTitle = "🔥 Các Series Nổi Bật";
  if (searchQuery) {
    sectionTitle = `🔍 Kết quả tìm kiếm: "${searchQuery}"`;
  } else if (currentGenre) {
    sectionTitle = `📚 Thể loại: ${currentGenre}`;
  }

  return (
    <div className="home-container">
      <Navbar />
      <div className="vote-banner">
        <h1 className="banner-title">ĐẠI CHIẾN MANGA: BẢNG XẾP HẠNG TUẦN</h1>
        <p className="banner-subtitle">
          Mỗi lượt <span className="highlight-text">VOTE</span> là một lá phiếu
          định đoạt danh tiếng của Series. Series nào sẽ thống trị đỉnh bảng?
          Series nào sẽ bị lãng quên? Quyền năng đó nằm trong tay bạn. Hãy hành
          động ngay!
        </p>
      </div>
      <div className="main-content">
        <section>
          <h2 className="section-title">{sectionTitle}</h2>
          <div className="cards-grid">
            {displaySeries.length > 0 ? (
              displaySeries.map((series) => (
                <SeriesCard
                  key={series.id}
                  series={series}
                  onClick={(clickedSeries) => setSelectedSeries(clickedSeries)}
                />
              ))
            ) : (
              <div className="empty-state">
                <h3 className="empty-title">
                  Không tìm thấy kết quả nào phù hợp
                </h3>
                <p className="empty-subtitle">Vui lòng thử lại.</p>
              </div>
            )}
          </div>
        </section>
      </div>
      {selectedSeries && (
        <SeriesModal
          series={selectedSeries}
          onClose={() => setSelectedSeries(null)}
        />
      )}
    </div>
  );
};

export default HomePage;
