import React from 'react';
import Navbar from '../components/Navbar';
import { trendingSeries } from '../data/mockData';
import '../styles/RankingPage.css';

const RankingPage = () => {
  const rankedSeries = [...trendingSeries].sort((a, b) => {
    const totalVotesA = a.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
    const totalVotesB = b.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
    return totalVotesB - totalVotesA;
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
        <p>Cập nhật liên tục dựa trên lượt VOTE của độc giả</p>
      </div>

      <div className="ranking-list">
        {rankedSeries.map((series, index) => {
          const totalVotes = series.chapters?.reduce((sum, ch) => sum + ch.votes, 0) || 0;
          
          return (
            <div key={series.id} className="ranking-item">
              <div className="rank-number">
                {getRankBadge(index)}
              </div>
              
              <img src={series.coverUrl} alt={series.title} className="ranking-cover" />
              
              <div className="ranking-info">
                <h2>{series.title}</h2>
                <p className="ranking-author">Tác giả: {series.author}</p>
                <div className="ranking-genres">
                  {series.genres?.map((genre, i) => (
                    <span key={i} className="genre-tag">{genre}</span>
                  ))}
                </div>
              </div>
              
              <div className="ranking-votes">
                <span className="vote-count">{totalVotes.toLocaleString()}</span>
                <span className="vote-label">VOTE</span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default RankingPage;