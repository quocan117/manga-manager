import React from "react";
import "../styles/SeriesCard.css";

const SeriesCard = ({ series, onClick }) => {
  return (
    <div className="series-card" onClick={() => onClick(series)}>
      <img src={series.coverUrl} alt={series.title} className="series-cover" />
      <div className="series-info">
        <h3 className="series-name">{series.title}</h3>
        <p className="series-author">{series.author}</p>
      </div>
    </div>
  );
};

export default SeriesCard;
