import React, { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import "../styles/RankingPage.css";
import FloatingMenu from "../components/FloatingMenu";

const RankingPage = () => {
  const [seriesList, setSeriesList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchRankingSeries = async () => {
      try {
        const response = await fetch("http://localhost:8080/manga-series", {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });
        if (response.ok) {
          const data = await response.json();
          setSeriesList(data);
        } else {
          console.error(
            "Lỗi server khi lấy dữ liệu xếp hạng, Mã lỗi:",
            response.status,
          );
        }
      } catch (error) {
        console.error("Lỗi kết nối mạng khi tải bảng xếp hạng:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchRankingSeries();
  }, []);

  const rankedSeries = [...seriesList].sort((a, b) => {
    const totalLikesA =
      a.totalLikes ?? (a.chapters?.reduce((sum, ch) => sum + ch.likes, 0) || 0);
    const totalLikesB =
      b.totalLikes ?? (b.chapters?.reduce((sum, ch) => sum + ch.likes, 0) || 0);
    return totalLikesB - totalLikesA;
  });

  const getRankBadge = (index) => {
    if (index === 0) return <span className="rank-badge gold">🏆 Top 1</span>;
    if (index === 1) return <span className="rank-badge silver">🥈 Top 2</span>;
    if (index === 2) return <span className="rank-badge bronze">🥉 Top 3</span>;
    return <span className="rank-badge normal">#{index + 1}</span>;
  };
  
  return (
    <div className="ranking-container">
      <Navbar />
      <div className="ranking-header">
        <h1>BẢNG XẾP HẠNG MANGA STUDIO</h1>
        <p>Cập nhật liên tục dựa trên lượt LIKE của độc giả</p>
      </div>
      <div className="reader-ranking-list">
        {rankedSeries.map((series, index) => {
          const totalLikes =
            series.chapters?.reduce((sum, ch) => sum + ch.likes, 0) || 0;
          return (
            <div key={series.id} className="reader-ranking-item">
              <div className="rank-number">{getRankBadge(index)}</div>
              <img
                src={
                  series.coverUrl
                    ? `http://localhost:8080/covers/${series.coverUrl}`
                    : "https://placehold.co/200x280/cccccc/ffffff?text=No+Image"
                }
                alt={series.title}
                className="ranking-cover"
              />
              <div className="ranking-info">
                <h2>{series.title}</h2>
                <p className="ranking-author">Tác giả: {series.author}</p>
                <div className="ranking-genres">
                  {series.genres?.map((genre, i) => (
                    <span key={i} className="genre-tag">
                      {genre}
                    </span>
                  ))}
                </div>
              </div>
              <div className="ranking-likes">
                <span className="like-count">
                  {totalLikes.toLocaleString()}
                </span>
                <span className="like-label">LIKE</span>
              </div>
            </div>
          );
        })}
      </div>
      <FloatingMenu />
    </div>
  );
};
export default RankingPage;