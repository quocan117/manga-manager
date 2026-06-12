import React, { useState } from "react";
import "../styles/SeriesModal.css";

const SeriesModal = ({ series, onClose }) => {
  const [userVotes, setUserVotes] = useState({});
  const [filter, setFilter] = useState("");
  const handleToggleVote = (chapterId) => {
    setUserVotes((prev) => ({
      ...prev,
      [chapterId]: !prev[chapterId],
    }));
  };

  if (!series) return null;
  const filteredChapters =
    filter === ""
      ? []
      : series.chapters.filter((c) => c.id.toString() === filter);
  return (
    <div className="custom-modal-overlay" onClick={onClose}>
      <div
        className="custom-modal-content"
        onClick={(e) => e.stopPropagation()}
      >
        <button className="close-btn" onClick={onClose}>
          ✖
        </button>
        <div className="custom-modal-header">
          <img
            src={series.coverUrl}
            alt={series.title}
            className="modal-cover"
          />
          <div className="modal-info">
            <h2>{series.title}</h2>
            <p>
              <strong>Tác giả:</strong> {series.author}
            </p>
            <p className="modal-desc">{series.description}</p>
          </div>
        </div>
        <div className="modal-chapters">
          <div className="chapter-filter-header">
            <h3>Danh sách chương</h3>

            <select
              className="chapter-filter-select"
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            >
              <option value="" disabled>
                -- Vui lòng chọn một chương --
              </option>
              {series.chapters.map((chap) => (
                <option key={chap.id} value={chap.id}>
                  {chap.title}
                </option>
              ))}
            </select>
          </div>
          <div className="chapter-list">
            {filteredChapters.map((chapter) => {
              const hasVoted = userVotes[chapter.id];
              const displayVotes = hasVoted ? chapter.votes + 1 : chapter.votes;
              return (
                <div key={chapter.id} className="chapter-item">
                  <span className="chapter-title">{chapter.title}</span>
                  <button
                    className={`modal-vote-btn ${hasVoted ? "voted" : ""}`}
                    onClick={() => handleToggleVote(chapter.id)}
                  >
                    {hasVoted ? "❤️ Đã Vote" : "🤍 Bình chọn"} ({displayVotes})
                  </button>
                </div>
              );
            })}
            {filter === "" && (
              <div
                style={{
                  textAlign: "center",
                  padding: "40px 0",
                  color: "#888",
                  fontStyle: "italic",
                }}
              >
                👈 Hãy chọn một chương từ menu phía trên để tiến hành bình chọn.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SeriesModal;
