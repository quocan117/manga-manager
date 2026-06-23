import React, { useState, useEffect } from "react";

const RankingDecisionPage = () => {
  const [seriesList, setSeriesList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const [searchQuery, setSearchQuery] = useState("");
  const [filterOption, setFilterOption] = useState("all"); 

  useEffect(() => {
    const fetchRankingForBoard = async () => {
      try {
        const response = await fetch("http://localhost:8080/manga-series");
        if (response.ok) {
          const data = await response.json();

          const processedData = data.map((series) => {
            const totalLikes =
              series.chapters?.reduce((sum, ch) => sum + ch.likes, 0) || 0;
            return { ...series, totalLikes };
          });

          processedData.sort((a, b) => a.totalLikes - b.totalLikes);

          const rankedData = processedData.map((series, index) => ({
            ...series,
            globalRank: processedData.length - index,
            isDanger: index < 3,
          }));

          setSeriesList(rankedData);
        } else {
          console.error("Lỗi khi tải dữ liệu xếp hạng.");
        }
      } catch (error) {
        console.error("Lỗi kết nối API:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchRankingForBoard();
  }, []);

  const handleCancelSeries = async (id, title) => {
    if (
      window.confirm(
        `XÁC NHẬN: Bạn có chắc chắn muốn ĐÌNH BẢN bộ truyện "${title}"? Tác phẩm sẽ bị gỡ khỏi nền tảng độc giả.`,
      )
    ) {
      setSeriesList((prevList) =>
        prevList.map((s) => (s.id === id ? { ...s, status: "CANCELLED" } : s)),
      );
      alert(`Đã ra quyết định đình bản bộ truyện "${title}".`);
    }
  };

  if (isLoading)
    return (
      <div className="tab-content">
        <h2>Đang tải dữ liệu...</h2>
      </div>
    );

  const activeSeriesList = seriesList.filter((s) => s.status !== "CANCELLED");

  const displayedSeries = activeSeriesList.filter((series) => {
    const matchesSearch =
      series.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (series.author &&
        series.author.toLowerCase().includes(searchQuery.toLowerCase()));

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
        Theo dõi chỉ số tương tác định kỳ để ra quyết định duy trì phát hành
        hoặc đình bản tác phẩm.
      </p>

      <div className="dashboard-summary">
        <div className="summary-card">
          <h3>Tổng Series Xuất Bản</h3>
          <p className="summary-value">{totalSeries}</p>
        </div>
        <div className="summary-card">
          <h3>Tổng Tương Tác Hệ Thống</h3>
          <p className="summary-value">{totalPlatformLikes.toLocaleString()}</p>
        </div>
        <div className="summary-card danger-card">
          <h3>Series Cần Đánh Giá Lại</h3>
          <p className="summary-value">{atRiskCount}</p>
        </div>
      </div>

      <div
        className="board-header"
        style={{ marginBottom: "15px", alignItems: "flex-end" }}
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

        <input
          type="text"
          placeholder="🔍 Tìm tên truyện, tác giả..."
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
              <th>Tác giả</th>
              <th>Tổng Lượt Thích</th>
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
                  <td>{series.author || "Chưa cập nhật"}</td>
                  <td>{series.totalLikes.toLocaleString()}</td>
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
                  colSpan="6"
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
