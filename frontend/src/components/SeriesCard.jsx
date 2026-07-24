import React from "react";
import "../styles/SeriesCard.css";

const SeriesCard = ({ series, onClick }) => {
  const isComingSoon = series.status === "Coming Soon";

  return (
    <div
      className={`series-card ${isComingSoon ? "series-card-coming-soon" : ""}`}
      onClick={() => onClick(series)}
    >
      <div className="card-cover-wrapper">
        <img
          src={
            series.coverUrl
              ? `http://localhost:8080/covers/${series.coverUrl}`
              : "https://placehold.co/200x280/cccccc/ffffff?text=No+Image"
          }
          alt={series.title}
          className="card-cover"
        />
        {isComingSoon && <span className="coming-soon-badge">Sắp ra mắt</span>}
      </div>
      <div className="series-info">
        <h3 className="series-name">{series.title}</h3>
        <p className="series-author">{series.author}</p>
      </div>
    </div>
  );
};

export default SeriesCard;
