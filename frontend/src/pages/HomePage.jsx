import React, { useState } from "react";
import Navbar from "../components/Navbar";
import SeriesCard from "../components/SeriesCard";
import SeriesModal from "../components/SeriesModal";
import { trendingSeries } from "../data/mockData";
import "../styles/HomePage.css";

const HomePage = () => {
  const [selectedSeries, setSelectedSeries] = useState(null);

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
          <h2 className="section-title">🔥 Các Series Nổi Bật</h2>
          <div className="cards-grid">
            {trendingSeries.map((series) => (
              <SeriesCard
                key={series.id}
                series={series}
                onClick={(clickedSeries) => setSelectedSeries(clickedSeries)}
              />
            ))}
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
