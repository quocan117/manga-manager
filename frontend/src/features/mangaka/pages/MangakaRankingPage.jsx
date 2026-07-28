import React, { useState, useEffect } from "react";
import {
  getMySeries,
  getSeriesFeedbackHistory,
  getSeriesRankingSummary,
  getSeriesChapters,
  getRankings,
} from "../../../services/mangakaService";
import { formatDateTime, formatDateOnly } from "../../../utils/formatDate";
import "../styles/DashboardMangaka.css";

function PeriodTrendChart({ periods, selectedImportId }) {
  const sorted = [...periods].sort(
    (a, b) => new Date(a.periodStart) - new Date(b.periodStart),
  );
  const max = Math.max(4, ...sorted.map((p) => p.voteCount || 0));
  const barWidth = 44;
  const gap = 28;
  const topGap = 28;
  const barAreaHeight = 140;
  const width = sorted.length * (barWidth + gap) + gap;
  const svgHeight = topGap + barAreaHeight + 34;
  const baselineY = topGap + barAreaHeight;
  return (
    <svg
      viewBox={`0 0 ${width} ${svgHeight}`}
      className="bar-chart-svg"
      role="img"
      aria-label="Biểu đồ lượt vote theo từng kỳ tổng hợp"
    >
      {[0, 0.25, 0.5, 0.75, 1].map((step) => {
        const y = topGap + barAreaHeight * (1 - step);
        return (
          <g key={step}>
            <line x1="0" y1={y} x2={width} y2={y} className="bar-chart-grid" />
            <text x="2" y={y - 3} className="bar-axis-text">
              {Math.round(max * step)}
            </text>
          </g>
        );
      })}
      {sorted.map((p, i) => {
        const value = p.voteCount || 0;
        const h = (value / max) * barAreaHeight;
        const x = gap + i * (barWidth + gap);
        const y = baselineY - h;
        const isSelected =
          p.importId.toString() === selectedImportId?.toString();
        return (
          <g key={p.importId}>
            <rect
              x={x}
              y={y}
              width={barWidth}
              height={Math.max(h, 2)}
              rx="6"
              fill={isSelected ? "#3498db" : "#aed0ec"}
            />
            <text
              x={x + barWidth / 2}
              y={y - 8}
              textAnchor="middle"
              className="bar-value-text"
            >
              {value}
            </text>
            <text
              x={x + barWidth / 2}
              y={baselineY + 18}
              textAnchor="middle"
              className="bar-label-text"
            >
              {formatDateOnly(p.periodStart)}
            </text>
          </g>
        );
      })}
    </svg>
  );
}

export default function MangakaRankingPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [seriesLoading, setSeriesLoading] = useState(true);
  const [seriesError, setSeriesError] = useState("");

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedSeriesId, setSelectedSeriesId] = useState("");

  const [historyPeriods, setHistoryPeriods] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyError, setHistoryError] = useState("");
  const [selectedHistoryId, setSelectedHistoryId] = useState("");

  const [dashboardData, setDashboardData] = useState(null);
  const [dashboardLoading, setDashboardLoading] = useState(false);
  const [dashboardError, setDashboardError] = useState("");

  const fetchSeries = async () => {
    try {
      setSeriesLoading(true);
      setSeriesError("");
      const data = await getMySeries();
      setSeriesList(data || []);
    } catch (error) {
      console.error("Lỗi tải danh sách Series:", error);
      setSeriesError(
        "Không thể tải danh sách Series của bạn. Vui lòng thử lại.",
      );
    } finally {
      setSeriesLoading(false);
    }
  };

  useEffect(() => {
    fetchSeries();
  }, []);

  useEffect(() => {
    setDashboardData(null);
    setDashboardError("");

    if (!selectedSeriesId) {
      setHistoryPeriods([]);
      setSelectedHistoryId("");
      setHistoryError("");
      return;
    }

    const fetchHistory = async () => {
      try {
        setHistoryLoading(true);
        setHistoryError("");
        const data = await getSeriesFeedbackHistory(selectedSeriesId);
        setHistoryPeriods(data || []);
        setSelectedHistoryId(data && data.length > 0 ? data[0].importId : "");
      } catch (error) {
        console.error("Lỗi tải lịch sử tổng hợp:", error);
        setHistoryError("Không thể tải lịch sử tổng hợp cho Series này.");
        setHistoryPeriods([]);
        setSelectedHistoryId("");
      } finally {
        setHistoryLoading(false);
      }
    };
    fetchHistory();
  }, [selectedSeriesId]);

  useEffect(() => {
    setDashboardData(null);
    setDashboardError("");
  }, [selectedHistoryId]);

  const filteredSeries = seriesList.filter((s) =>
    s.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const selectedSeries = seriesList.find(
    (s) => s.id.toString() === selectedSeriesId.toString(),
  );

  const handleAggregate = async () => {
    if (!selectedSeriesId || !selectedHistoryId) return;
    const period = historyPeriods.find(
      (h) => h.importId.toString() === selectedHistoryId.toString(),
    );
    if (!period) return;
    try {
      setDashboardLoading(true);
      setDashboardError("");
      const seriesIdNum = Number(selectedSeriesId);
      const [summary, ownRankings, chaptersList] = await Promise.all([
        getSeriesRankingSummary(
          selectedSeriesId,
          period.periodStart,
          period.periodEnd,
        ),
        getRankings(),
        getSeriesChapters(selectedSeriesId),
      ]);

      const allTimeTotalVotes = (ownRankings || [])
        .filter((r) => r.seriesId === seriesIdNum)
        .reduce((sum, r) => sum + (r.voteCount || 0), 0);

      const periodStartMs = new Date(period.periodStart).getTime();
      const periodEndMs = new Date(period.periodEnd).getTime();
      const chaptersInPeriod = (chaptersList || []).filter((c) => {
        if (!c.releaseDate) return false;
        const t = new Date(c.releaseDate).getTime();
        return t >= periodStartMs && t <= periodEndMs;
      });

      const sortedPeriods = [...historyPeriods].sort(
        (a, b) => new Date(a.periodStart) - new Date(b.periodStart),
      );
      const currentIndex = sortedPeriods.findIndex(
        (p) => p.importId.toString() === selectedHistoryId.toString(),
      );
      const previousPeriod =
        currentIndex > 0 ? sortedPeriods[currentIndex - 1] : null;

      const voteCount = summary?.voteCount ?? period.voteCount ?? 0;

      let trendDiff = null;
      if (previousPeriod) {
        trendDiff = voteCount - (previousPeriod.voteCount || 0);
      }

      setDashboardData({
        period,
        voteCount,
        allTimeTotalVotes,
        chaptersInPeriod,
        trendDiff,
      });
    } catch (error) {
      console.error("Lỗi tổng hợp dữ liệu Dashboard:", error);
      setDashboardError(
        error?.response?.data?.message ||
          "Không thể tải dữ liệu thống kê lúc này. Vui lòng thử lại.",
      );
    } finally {
      setDashboardLoading(false);
    }
  };

  return (
    <div className="tab-content">
      <h2>Phân Tích Dữ Liệu Series</h2>
      <p className="text-muted">
        Chọn Series và kỳ tổng hợp, sau đó bấm <strong>“Tổng hợp”</strong> để
        xem thống kê xếp hạng, lượt vote và các chương đã phát hành trong kỳ.
      </p>

      {seriesError && (
        <div className="alert alert-danger">
          {seriesError}{" "}
          <button
            className="btn btn-outline-danger btn-sm ms-2"
            onClick={fetchSeries}
          >
            Thử lại
          </button>
        </div>
      )}

      <div className="row mb-4">
        <div className="col-md-6">
          <label className="form-label fw-bold">1. Chọn Series của bạn</label>
          <input
            type="text"
            className="form-control mb-2"
            placeholder="🔍 Tìm tên Series..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            disabled={seriesLoading}
          />
          <select
            className="form-select"
            value={selectedSeriesId}
            onChange={(e) => setSelectedSeriesId(e.target.value)}
            disabled={seriesLoading}
          >
            <option value="">
              {seriesLoading
                ? "Đang tải danh sách Series..."
                : seriesList.length === 0
                  ? "Bạn chưa có Series nào"
                  : "-- Chọn Series --"}
            </option>
            {filteredSeries.map((s) => (
              <option key={s.id} value={s.id}>
                {s.title}
              </option>
            ))}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label fw-bold">
            2. Khoảng thời gian tổng hợp
          </label>
          <select
            className="form-select mb-2"
            value={selectedHistoryId}
            onChange={(e) => setSelectedHistoryId(e.target.value)}
            disabled={
              !selectedSeriesId || historyLoading || historyPeriods.length === 0
            }
          >
            {historyLoading && <option value="">Đang tải dữ liệu...</option>}
            {!historyLoading && historyPeriods.length === 0 && (
              <option value="">Chưa có dữ liệu tổng hợp nào</option>
            )}
            {historyPeriods.map((h) => (
              <option key={h.importId} value={h.importId}>
                {formatDateTime(h.periodStart)} ➔ {formatDateTime(h.periodEnd)}
                {" · "}
                {h.voteCount.toLocaleString()} vote
              </option>
            ))}
          </select>
          <button
            className="btn btn-primary w-100 fw-bold"
            onClick={handleAggregate}
            disabled={
              !selectedSeriesId ||
              !selectedHistoryId ||
              historyLoading ||
              dashboardLoading
            }
          >
            {dashboardLoading ? "Đang tổng hợp..." : "📊 Tổng hợp"}
          </button>
        </div>
      </div>

      {historyError && <div className="alert alert-danger">{historyError}</div>}
      {selectedSeriesId && !historyLoading && historyPeriods.length === 0 && (
        <div className="alert alert-info">
          Series này chưa có dữ liệu tổng hợp nào từ Hội đồng biên tập.
        </div>
      )}

      {dashboardError && (
        <div className="alert alert-danger">{dashboardError}</div>
      )}

      {dashboardLoading && (
        <div className="text-center my-5">
          <div className="spinner-border text-primary" />
          <p className="mt-3 text-muted">Đang tải Dashboard thống kê...</p>
        </div>
      )}

      {!dashboardLoading &&
        !dashboardData &&
        !dashboardError &&
        selectedHistoryId && (
          <div className="chart-card text-center py-5">
            <p className="empty-text mb-0">
              Bấm “Tổng hợp” để xem Dashboard thống kê cho kỳ đã chọn.
            </p>
          </div>
        )}

      {!dashboardLoading && dashboardData && (
        <div>
          <h4 className="mb-1">{selectedSeries?.title}</h4>
          <p className="text-muted mb-3">
            Kỳ: {formatDateTime(dashboardData.period.periodStart)} ➔{" "}
            {formatDateTime(dashboardData.period.periodEnd)}
          </p>

          <div className="row g-4">
            <div className="col-md-4">
              <div className="kpi-card">
                <div className="kpi-icon kpi-icon-total">🗳️</div>
                <div>
                  <div className="kpi-label">Vote trong kỳ</div>
                  <div className="kpi-value">
                    {dashboardData.voteCount.toLocaleString()}
                  </div>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="kpi-card">
                <div className="kpi-icon kpi-icon-pending">📚</div>
                <div>
                  <div className="kpi-label">Tổng vote mọi kỳ</div>
                  <div className="kpi-value">
                    {dashboardData.allTimeTotalVotes.toLocaleString()}
                  </div>
                </div>
              </div>
            </div>

            <div className="col-md-4">
              <div className="kpi-card">
                <div
                  className={`kpi-icon ${
                    dashboardData.trendDiff === null
                      ? "kpi-icon-pending"
                      : dashboardData.trendDiff >= 0
                        ? "kpi-icon-published"
                        : "kpi-icon-alert"
                  }`}
                >
                  {dashboardData.trendDiff === null
                    ? "➖"
                    : dashboardData.trendDiff >= 0
                      ? "▲"
                      : "▼"}
                </div>
                <div>
                  <div className="kpi-label">So với kỳ trước</div>
                  <div className="kpi-value">
                    {dashboardData.trendDiff === null
                      ? "N/A"
                      : `${dashboardData.trendDiff > 0 ? "+" : ""}${dashboardData.trendDiff} vote`}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="row g-4 mt-1">
            <div className="col-12">
              <div className="chart-card h-100">
                <div className="chart-card-title">
                  Xu hướng vote qua các kỳ đã tổng hợp
                </div>
                {historyPeriods.length <= 1 ? (
                  <p className="empty-text">
                    Cần ít nhất 2 kỳ tổng hợp để xem xu hướng.
                  </p>
                ) : (
                  <div className="bar-chart-wrap">
                    <PeriodTrendChart
                      periods={historyPeriods}
                      selectedImportId={selectedHistoryId}
                    />
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="chart-card mt-4">
            <div className="chart-card-title">
              Chương phát hành trong kỳ ({dashboardData.chaptersInPeriod.length}
              )
            </div>
            {dashboardData.chaptersInPeriod.length === 0 ? (
              <p className="empty-text mb-0">
                Không có chương nào được phát hành trong khoảng thời gian này.
              </p>
            ) : (
              <table className="table table-sm align-middle mb-0">
                <thead>
                  <tr>
                    <th>Chương</th>
                    <th>Tiêu đề</th>
                    <th>Trạng thái</th>
                    <th>Ngày phát hành</th>
                  </tr>
                </thead>
                <tbody>
                  {dashboardData.chaptersInPeriod.map((c) => (
                    <tr key={c.id}>
                      <td>#{c.chapterNumber}</td>
                      <td>{c.title}</td>
                      <td>{c.status}</td>
                      <td>{formatDateOnly(c.releaseDate)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
