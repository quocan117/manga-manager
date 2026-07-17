import React, { useState, useEffect, useMemo } from "react";
import {
  cancelSeries,
  getRankings,
  getRankingPeriods,
  getSeriesTotalVotes,
} from "../../../services/boardService";

const DANGER_ZONE_SIZE = 3;
const RankingDecisionPage = () => {
  const [rankings, setRankings] = useState([]);
  const [periods, setPeriods] = useState([]);
  const [selectedPeriod, setSelectedPeriod] = useState("");
  const [totalVotesMap, setTotalVotesMap] = useState({});
  const [cancelledIds, setCancelledIds] = useState(new Set());
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterOption, setFilterOption] = useState("all");

  useEffect(() => {
    const fetchMeta = async () => {
      try {
        const [periodList, totalVotes] = await Promise.all([
          getRankingPeriods(),
          getSeriesTotalVotes(),
        ]);
        const list = periodList || [];
        setPeriods(list);
        if (list.length > 0) setSelectedPeriod(list[0]);
        const map = {};
        (totalVotes || []).forEach((v) => {
          map[v.seriesId] = v.totalVotes;
        });
        setTotalVotesMap(map);
      } catch (error) {
        console.error("Lỗi khi tải danh sách chu kỳ:", error);
      }
    };
    fetchMeta();
  }, []);

  useEffect(() => {
    const fetchRankingForBoard = async () => {
      setIsLoading(true);
      try {
        const data = await getRankings(selectedPeriod || undefined);
        setRankings(data || []);
      } catch (error) {
        console.error("Lỗi khi tải dữ liệu xếp hạng:", error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchRankingForBoard();
  }, [selectedPeriod]);

  const latestRankingPerSeries = useMemo(() => {
    const seen = new Map();
    for (const r of rankings) {
      if (!seen.has(r.seriesId)) {
        seen.set(r.seriesId, r);
      }
    }
    return Array.from(seen.values()).sort(
      (a, b) => (a.position ?? 0) - (b.position ?? 0),
    );
  }, [rankings]);

  const processedSeries = useMemo(() => {
    const total = latestRankingPerSeries.length;
    return latestRankingPerSeries.map((r, index) => ({
      id: r.seriesId,
      title: r.seriesTitle,
      totalLikes: r.voteCount ?? 0,
      totalLikesAllTime: totalVotesMap[r.seriesId] ?? r.voteCount ?? 0,
      globalRank: r.position ?? index + 1,
      period: r.period,
      calculatedAt: r.calculatedAt,
      isDanger: index >= total - DANGER_ZONE_SIZE && total > 0,
      isCancelled: cancelledIds.has(r.seriesId),
    }));
  }, [latestRankingPerSeries, cancelledIds, totalVotesMap]);

  const handleCancelSeries = async (id, title) => {
    if (
      window.confirm(
        `XÁC NHẬN: Bạn có chắc chắn muốn ĐÌNH BẢN bộ truyện "${title}"?`,
      )
    ) {
      try {
        await cancelSeries(id);
        setCancelledIds((prev) => new Set(prev).add(id));
        alert(`Đã ra quyết định đình bản bộ truyện "${title}".`);
      } catch (error) {
        console.error("Lỗi khi đình bản:", error);
        alert(
          "Không thể đình bản truyện lúc này. Có thể truyện đã bị hủy trước đó.",
        );
      }
    }
  };

  if (isLoading)
    return (
      <div className="tab-content">
        <h2>Đang tải dữ liệu...</h2>
      </div>
    );

  const activeSeriesList = processedSeries.filter((s) => !s.isCancelled);

  const displayedSeries = activeSeriesList.filter((series) => {
    const matchesSearch = series.title
      ?.toLowerCase()
      .includes(searchQuery.toLowerCase());
    const matchesFilter =
      filterOption === "all"
        ? true
        : filterOption === "danger"
          ? series.isDanger
          : !series.isDanger;
    return matchesSearch && matchesFilter;
  });

  const totalSeries = activeSeriesList.length;

  const totalPlatformLikes = activeSeriesList.reduce(
    (sum, s) => sum + s.totalLikes,
    0,
  );
  
  const atRiskCount = activeSeriesList.filter((s) => s.isDanger).length;

  return (
    <div className="tab-content">
      <h2>Bảng Xếp Hạng & Đánh Giá Năng Lực Series</h2>
      <p>
        Dữ liệu được tổng hợp tự động từ lượt bình chọn (like) của độc giả sau
        mỗi kỳ nhập liệu. Dựa vào đó để ra quyết định duy trì phát hành hoặc
        đình bản tác phẩm.
      </p>
      {periods.length === 0 && (
        <div className="empty-state" style={{ marginBottom: 20 }}>
          <h3 className="empty-title">Chưa có dữ liệu xếp hạng</h3>
          <p className="empty-subtitle">
            Vào mục "Bình Chọn Độc Giả" để tổng hợp dữ liệu của kỳ mới nhất
            trước khi xem bảng xếp hạng tại đây.
          </p>
        </div>
      )}
      <div className="dashboard-summary">
        <div className="summary-card">
          <h3>Tổng Series Xuất Bản</h3>
          <p className="summary-value">{totalSeries}</p>
        </div>
        <div className="summary-card">
          <h3>Tổng Tương Tác Kỳ {selectedPeriod || "—"}</h3>
          <p className="summary-value">{totalPlatformLikes.toLocaleString()}</p>
        </div>
        <div className="summary-card danger-card">
          <h3>Series Cần Đánh Giá Lại</h3>
          <p className="summary-value">{atRiskCount}</p>
        </div>
      </div>
      <div
        className="board-header"
        style={{
          marginBottom: "15px",
          alignItems: "flex-end",
          gap: "12px",
          flexWrap: "wrap",
        }}
      >
        <div
          className="board-tabs"
          style={{ marginBottom: 0, borderBottom: "none", gap: "10px" }}
        >
          <button
            className={`tab-btn ${filterOption === "all" ? "active" : ""}`}
            onClick={() => setFilterOption("all")}
          >
            Toàn bộ danh sách
          </button>
          <button
            className={`tab-btn ${filterOption === "danger" ? "active" : ""}`}
            style={
              filterOption === "danger"
                ? { color: "#c0392b", borderBottomColor: "#c0392b" }
                : {}
            }
            onClick={() => setFilterOption("danger")}
          >
            Cảnh báo đình bản
          </button>
          <button
            className={`tab-btn ${filterOption === "safe" ? "active" : ""}`}
            style={
              filterOption === "safe"
                ? { color: "#27ae60", borderBottomColor: "#27ae60" }
                : {}
            }
            onClick={() => setFilterOption("safe")}
          >
            Xuất bản ổn định
          </button>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
          <label
            htmlFor="period-select"
            style={{ fontSize: "0.9rem", color: "#555" }}
          >
            Chu kỳ:
          </label>
          <select
            id="period-select"
            className="search-input-board"
            value={selectedPeriod}
            onChange={(e) => setSelectedPeriod(e.target.value)}
            style={{ minWidth: "140px" }}
          >
            {periods.length === 0 && <option value="">Chưa có kỳ nào</option>}
            {periods.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </div>
        <input
          type="text"
          placeholder="🔍 Tìm tên truyện..."
          className="search-input-board"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>
      <div className="table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Hạng</th>
              <th>Tên Series</th>
              <th>Kỳ Tổng Hợp</th>
              <th>Bình Chọn Kỳ Này</th>
              <th>Tổng Bình Chọn (Từ Khi Phát Hành)</th>
              <th>Đánh Giá Năng Lực</th>
              <th>Quyết Định</th>
            </tr>
          </thead>
          <tbody>
            {displayedSeries.length > 0 ? (
              displayedSeries.map((series) => (
                <tr
                  key={series.id}
                  className={series.isDanger ? "row-danger" : ""}
                >
                  <td>#{series.globalRank}</td>
                  <td>
                    <strong>{series.title}</strong>
                  </td>
                  <td>{series.period || "—"}</td>
                  <td>{series.totalLikes.toLocaleString()}</td>
                  <td>{series.totalLikesAllTime.toLocaleString()}</td>
                  <td>
                    {series.isDanger ? (
                      <span className="badge badge-danger">
                        Cảnh báo đình bản
                      </span>
                    ) : (
                      <span className="badge badge-success">
                        Xuất bản ổn định
                      </span>
                    )}
                  </td>
                  <td>
                    {series.isDanger ? (
                      <button
                        className="btn-cancel-series"
                        onClick={() =>
                          handleCancelSeries(series.id, series.title)
                        }
                      >
                        Quyết định đình bản
                      </button>
                    ) : (
                      <span
                        style={{
                          color: "#7f8c8d",
                          fontStyle: "italic",
                          fontSize: "0.9rem",
                        }}
                      >
                        Duy trì phát hành
                      </span>
                    )}
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td
                  colSpan="7"
                  style={{
                    textAlign: "center",
                    padding: "40px",
                    color: "#7f8c8d",
                  }}
                >
                  Không tìm thấy series nào khớp với yêu cầu.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
export default RankingDecisionPage;
