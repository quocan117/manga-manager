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
  const isFiltering = currentGenre || searchQuery;

  const top4Series = [...trendingSeries]
    .sort((a, b) => {
      const totalVotesA =
        a.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
      const totalVotesB =
        b.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
      return totalVotesB - totalVotesA;
    })
    .slice(0, 4);

  const top4Ids = top4Series.map((series) => series.id);
  const remainingSeries = trendingSeries.filter(
    (series) => !top4Ids.includes(series.id),
  );

  let filteredSeries = trendingSeries;
  let filterTitle = "";

  if (currentGenre) {
    filteredSeries = trendingSeries.filter((series) =>
      series.genres?.includes(currentGenre),
    );
    filterTitle = `📚 Thể loại: ${currentGenre} (${filteredSeries.length} series)`;
  } else if (searchQuery) {
    const lowerCaseQuery = searchQuery.toLowerCase();
    filteredSeries = trendingSeries.filter(
      (series) =>
        series.title.toLowerCase().includes(lowerCaseQuery) ||
        series.author.toLowerCase().includes(lowerCaseQuery),
    );
    filterTitle = `🔍 Kết quả tìm kiếm: "${searchQuery}" (${filteredSeries.length} series)`;
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
        {isFiltering ? (
          <section>
            <h2 className="section-title">{filterTitle}</h2>
            <div className="cards-grid">
              {filteredSeries.length > 0 ? (
                filteredSeries.map((series) => (
                  <SeriesCard
                    key={series.id}
                    series={series}
                    onClick={(clickedSeries) =>
                      setSelectedSeries(clickedSeries)
                    }
                  />
                ))
              ) : (
                <div className="empty-state">
                  <h3 className="empty-title">
                    Không tìm thấy kết quả nào phù hợp 😢
                  </h3>
                  <p className="empty-subtitle">
                    Vui lòng thử lại với từ khóa hoặc thể loại khác.
                  </p>
                </div>
              )}
            </div>
          </section>
        ) : (
          <>
            <section>
              <h2 className="section-title">🔥 Các Series Nổi Bật</h2>
              <div className="cards-grid">
                {top4Series.map((series) => (
                  <SeriesCard
                    key={`top-${series.id}`}
                    series={series}
                    onClick={(clickedSeries) =>
                      setSelectedSeries(clickedSeries)
                    }
                  />
                ))}
              </div>
            </section>

            <section style={{ marginTop: "50px" }}>
              <h2 className="section-title">📚 Khám Phá Các Series Khác</h2>
              <div className="cards-grid">
                {remainingSeries.map((series) => (
                  <SeriesCard
                    key={`all-${series.id}`}
                    series={series}
                    onClick={(clickedSeries) =>
                      setSelectedSeries(clickedSeries)
                    }
                  />
                ))}
              </div>
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
    </div>
  );
};

export default HomePage;
