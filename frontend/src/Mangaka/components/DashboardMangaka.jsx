import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getMySeries,
  getNotifications,
  getRankings,
} from "../../services/mangakaService";
import "../styles/DashboardMangaka.css";

// Map trạng thái backend -> nhãn hiển thị + nhóm + màu (đồng bộ với MyManga.jsx)
const STATUS_META = {
  DRAFT: { label: "Bản nháp", group: "draft", color: "#94a3b8" },
  PENDING_EDITOR: {
    label: "Chờ xác nhận",
    group: "reviewing",
    color: "#60a5fa",
  },
  TANTOU_REVIEW: {
    label: "Biên tập kiểm tra",
    group: "reviewing",
    color: "#60a5fa",
  },
  REVIEWING: {
    label: "Hội đồng xét duyệt",
    group: "reviewing",
    color: "#60a5fa",
  },
  SUBMITTED_TO_EDITOR: {
    label: "Chờ biên tập duyệt",
    group: "reviewing",
    color: "#60a5fa",
  },
  REVISION_REQUESTED: {
    label: "Yêu cầu chỉnh sửa",
    group: "revision",
    color: "#f97316",
  },
  APPROVED: {
    label: "Đã duyệt - chờ lịch",
    group: "approved",
    color: "#a78bfa",
  },
  PUBLISHED: { label: "Đã xuất bản", group: "published", color: "#22c55e" },
  CANCELLED: { label: "Đã huỷ", group: "cancelled", color: "#ef4444" },
};

const CHART_GROUPS = [
  { key: "draft", label: "Bản nháp", color: "#94a3b8" },
  { key: "reviewing", label: "Đang xét duyệt", color: "#60a5fa" },
  { key: "revision", label: "Chỉnh sửa", color: "#f97316" },
  { key: "approved", label: "Chờ lịch xuất bản", color: "#a78bfa" },
  { key: "published", label: "Đã xuất bản", color: "#22c55e" },
  { key: "cancelled", label: "Đã huỷ", color: "#ef4444" },
];

function StatusBadge({ status }) {
  const normalized = (status || "").toUpperCase();
  const meta = STATUS_META[normalized];
  const key = (status || "unknown").toLowerCase();
  return (
    <span className={`status-badge status-badge-${key}`}>
      {meta ? meta.label : status || "Không rõ"}
    </span>
  );
}

// Tách nhãn dài thành 2 dòng để không bị tràn ra ngoài cột
function splitBarLabel(label) {
  if (label.length <= 10) return [label];
  const words = label.split(" ");
  const mid = Math.ceil(words.length / 2);
  return [words.slice(0, mid).join(" "), words.slice(mid).join(" ")];
}

function BarChart({ data }) {
  // Luôn lấy tối thiểu 4 để tránh trường hợp 1 cột duy nhất chiếm hết chiều cao khi dữ liệu còn ít
  const max = Math.max(4, ...data.map((d) => d.value));
  const barWidth = 44;
  const gap = 28;
  const topGap = 28; // khoảng đệm phía trên để số không bị cắt mất
  const barAreaHeight = 120;
  const labelAreaHeight = 40;
  const width = data.length * (barWidth + gap) + gap;
  const svgHeight = topGap + barAreaHeight + labelAreaHeight;
  const baselineY = topGap + barAreaHeight;
  const gridSteps = [0, 0.25, 0.5, 0.75, 1];

  return (
    <svg
      viewBox={`0 0 ${width} ${svgHeight}`}
      className="bar-chart-svg"
      role="img"
      aria-label="Biểu đồ số lượng series theo trạng thái"
    >
      {gridSteps.map((step) => {
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
      {data.map((d, i) => {
        const h = (d.value / max) * barAreaHeight;
        const x = gap + i * (barWidth + gap);
        const y = baselineY - h;
        const labelLines = splitBarLabel(d.label);
        return (
          <g key={d.key}>
            <rect
              x={x}
              y={y}
              width={barWidth}
              height={Math.max(h, 2)}
              rx="6"
              fill={d.color}
            />
            <text
              x={x + barWidth / 2}
              y={y - 8}
              textAnchor="middle"
              className="bar-value-text"
            >
              {d.value}
            </text>
            {labelLines.map((line, li) => (
              <text
                key={li}
                x={x + barWidth / 2}
                y={baselineY + 18 + li * 13}
                textAnchor="middle"
                className="bar-label-text"
              >
                {line}
              </text>
            ))}
          </g>
        );
      })}
    </svg>
  );
}

// Chuỗi ngày gần nhất (mặc định 7 ngày) để dựng biểu đồ hoạt động
function getLastNDays(n) {
  const days = [];
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    d.setDate(d.getDate() - i);
    days.push(d);
  }
  return days;
}

// Vẽ đường cong mượt (kiểu wave) + vùng tô màu bên dưới, dùng Bezier bậc 3 giữa các điểm
function buildSmoothAreaPath(points, baselineY) {
  if (points.length === 0) return { line: "", area: "" };
  let line = `M ${points[0].x} ${points[0].y}`;
  for (let i = 1; i < points.length; i++) {
    const p0 = points[i - 1];
    const p1 = points[i];
    const cx = (p0.x + p1.x) / 2;
    line += ` C ${cx} ${p0.y}, ${cx} ${p1.y}, ${p1.x} ${p1.y}`;
  }
  const first = points[0];
  const last = points[points.length - 1];
  const area = `${line} L ${last.x} ${baselineY} L ${first.x} ${baselineY} Z`;
  return { line, area };
}

function ActivityAreaChart({ data }) {
  const width = 560;
  const height = 170;
  const paddingX = 30;
  const paddingTop = 20;
  const baselineY = height - 30;
  const max = Math.max(1, ...data.map((d) => d.count));
  const step = data.length > 1 ? (width - paddingX * 2) / (data.length - 1) : 0;

  const points = data.map((d, i) => ({
    x: paddingX + i * step,
    y: baselineY - (d.count / max) * (baselineY - paddingTop),
    count: d.count,
    label: d.label,
  }));

  const { line, area } = buildSmoothAreaPath(points, baselineY);

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      className="area-chart-svg"
      role="img"
      aria-label="Hoạt động thông báo 7 ngày gần đây"
    >
      <defs>
        <linearGradient id="activityGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#f97316" stopOpacity="0.55" />
          <stop offset="100%" stopColor="#f97316" stopOpacity="0.03" />
        </linearGradient>
      </defs>
      <line
        x1={paddingX}
        y1={baselineY}
        x2={width - paddingX}
        y2={baselineY}
        className="bar-chart-axis"
      />
      <path d={area} fill="url(#activityGradient)" stroke="none" />
      <path d={line} fill="none" className="area-line" />
      {points.map((p, i) => (
        <g key={i}>
          <circle cx={p.x} cy={p.y} r="3.5" className="area-point" />
          <text
            x={p.x}
            y={p.y - 10}
            textAnchor="middle"
            className="bar-value-text"
          >
            {p.count}
          </text>
          <text
            x={p.x}
            y={height - 8}
            textAnchor="middle"
            className="bar-label-text"
          >
            {p.label}
          </text>
        </g>
      ))}
    </svg>
  );
}

const WEEKDAY_LABELS = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

function MiniCalendar({ highlightDates }) {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  const firstDay = new Date(year, month, 1);
  const startWeekday = firstDay.getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < startWeekday; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);
  const monthLabel = today.toLocaleDateString("vi-VN", {
    month: "long",
    year: "numeric",
  });

  return (
    <div className="mini-calendar">
      <div className="mini-calendar-title">{monthLabel}</div>
      <div className="mini-calendar-grid mini-calendar-weekdays">
        {WEEKDAY_LABELS.map((w) => (
          <span key={w}>{w}</span>
        ))}
      </div>
      <div className="mini-calendar-grid">
        {cells.map((day, idx) => {
          if (day === null) return <span key={idx} />;
          const dateObj = new Date(year, month, day);
          const isToday = dateObj.toDateString() === today.toDateString();
          const hasActivity = highlightDates.has(dateObj.toDateString());
          return (
            <span
              key={idx}
              className={`mini-calendar-day${isToday ? " mini-calendar-today" : ""}${
                hasActivity ? " mini-calendar-active" : ""
              }`}
            >
              {day}
            </span>
          );
        })}
      </div>
      <div className="mini-calendar-legend">
        <span className="legend-dot mini-calendar-legend-dot" /> Ngày có thông
        báo mới
      </div>
    </div>
  );
}

export default function DashboardMangaka() {
  const [series, setSeries] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [rankings, setRankings] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const [seriesData, notificationData, rankingData] = await Promise.all([
        getMySeries(),
        getNotifications(),
        getRankings().catch(() => []), // ranking có thể chưa sẵn sàng/không bắt buộc
      ]);
      setSeries(seriesData || []);
      setNotifications(notificationData || []);
      setRankings(rankingData || []);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const totalSeries = series.length;

  const groupCounts = useMemo(() => {
    const counts = {
      draft: 0,
      reviewing: 0,
      revision: 0,
      approved: 0,
      published: 0,
      cancelled: 0,
    };
    series.forEach((item) => {
      const normalized = (item.status || "").toUpperCase();
      const meta = STATUS_META[normalized];
      if (meta) counts[meta.group] += 1;
    });
    return counts;
  }, [series]);

  const publishedCount = groupCounts.published;
  const pendingCount =
    groupCounts.draft +
    groupCounts.reviewing +
    groupCounts.revision +
    groupCounts.approved;
  const unreadNotifications = notifications.filter((n) => !n.isRead).length;

  const publishedPercent =
    totalSeries > 0 ? Math.round((publishedCount / totalSeries) * 100) : 0;

  const chartData = CHART_GROUPS.map((g) => ({
    key: g.key,
    label: g.label,
    color: g.color,
    value: groupCounts[g.key] || 0,
  }));

  const recentSeries = useMemo(() => {
    return [...series]
      .sort((a, b) => {
        if (a.createdAt && b.createdAt)
          return new Date(b.createdAt) - new Date(a.createdAt);
        return (b.id ?? 0) - (a.id ?? 0);
      })
      .slice(0, 5);
  }, [series]);

  const topRankings = useMemo(() => {
    return [...rankings]
      .sort((a, b) => (a.position ?? 0) - (b.position ?? 0))
      .slice(0, 5);
  }, [rankings]);

  const recentNotifications = notifications.slice(0, 5);

  const activityData = useMemo(() => {
    const days = getLastNDays(7);
    return days.map((d) => ({
      label: d.toLocaleDateString("vi-VN", { weekday: "short" }),
      count: notifications.filter(
        (n) =>
          n.createdAt &&
          new Date(n.createdAt).toDateString() === d.toDateString(),
      ).length,
    }));
  }, [notifications]);

  const notificationDateSet = useMemo(() => {
    const set = new Set();
    notifications.forEach((n) => {
      if (n.createdAt) set.add(new Date(n.createdAt).toDateString());
    });
    return set;
  }, [notifications]);

  if (loading) {
    return (
      <div className="text-center mt-5">
        <div className="spinner-border" />
        <p className="mt-3">Loading...</p>
      </div>
    );
  }

  return (
    <div>
      <h2 className="mb-4">Dashboard</h2>

      {/* KPI cards */}
      <div className="row g-4">
        <div className="col-md-3">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-total">📚</div>
            <div>
              <div className="kpi-label">Tổng số Series</div>
              <div className="kpi-value">{totalSeries}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-published">✅</div>
            <div>
              <div className="kpi-label">Đã xuất bản</div>
              <div className="kpi-value">{publishedCount}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-pending">⏳</div>
            <div>
              <div className="kpi-label">Đang xử lý</div>
              <div className="kpi-value">{pendingCount}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-alert">🔔</div>
            <div>
              <div className="kpi-label">Thông báo chưa đọc</div>
              <div className="kpi-value">{unreadNotifications}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Bar chart + Donut chart */}
      <div className="row g-4 mt-1">
        <div className="col-md-8">
          <div className="chart-card h-100">
            <div className="chart-card-title">Series theo trạng thái</div>
            {totalSeries === 0 ? (
              <p className="empty-text">Chưa có series nào.</p>
            ) : (
              <div className="bar-chart-wrap">
                <BarChart data={chartData} />
              </div>
            )}
          </div>
        </div>
        <div className="col-md-4">
          <div className="chart-card h-100">
            <div className="chart-card-title">Tỷ lệ xuất bản</div>
            <div className="donut-wrap">
              <div
                className="donut-ring"
                style={{
                  background: `conic-gradient(#22c55e ${publishedPercent}%, #e5e7eb ${publishedPercent}% 100%)`,
                }}
              >
                <div className="donut-center">
                  <span className="donut-percent">{publishedPercent}%</span>
                  <span className="donut-caption">Đã xuất bản</span>
                </div>
              </div>
              <ul className="legend-list">
                {CHART_GROUPS.map((g) => (
                  <li className="legend-item" key={g.key}>
                    <span>
                      <span
                        className="legend-dot"
                        style={{ background: g.color }}
                      />
                      {g.label}
                    </span>
                    <span>{groupCounts[g.key] || 0}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Recent series + Ranking */}
      <div className="row g-4 mt-1">
        <div className="col-md-8">
          <div className="chart-card h-100">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <div className="chart-card-title mb-0">My Recent Series</div>
              <button
                className="btn btn-sm btn-outline-primary"
                onClick={() => navigate("/mangaka/manga")}
              >
                Xem tất cả
              </button>
            </div>
            {recentSeries.length === 0 ? (
              <p className="empty-text">Chưa có series nào.</p>
            ) : (
              <table className="table table-sm align-middle mb-0">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Status</th>
                    <th>Ngày tạo</th>
                  </tr>
                </thead>
                <tbody>
                  {recentSeries.map((item) => (
                    <tr key={item.id}>
                      <td>{item.title}</td>
                      <td>
                        <StatusBadge status={item.status} />
                      </td>
                      <td>
                        {item.createdAt
                          ? new Date(item.createdAt).toLocaleDateString()
                          : "-"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
        <div className="col-md-4">
          <div className="chart-card h-100">
            <div className="chart-card-title">Bảng xếp hạng của tôi</div>
            {topRankings.length === 0 ? (
              <p className="empty-text">Chưa có dữ liệu xếp hạng.</p>
            ) : (
              <ul className="ranking-list">
                {topRankings.map((r) => (
                  <li className="ranking-item" key={r.id}>
                    <span className="ranking-pos">
                      {r.position === 1 && "🥇"}
                      {r.position === 2 && "🥈"}
                      {r.position === 3 && "🥉"}
                      {r.position > 3 && `#${r.position}`}
                    </span>
                    <span className="ranking-title">{r.seriesTitle}</span>
                    <span className="ranking-score">{r.score}</span>
                  </li>
                ))}
              </ul>
            )}
            <button
              className="btn btn-warning btn-sm panel-cta-btn"
              onClick={() => navigate("/mangaka/ranking")}
            >
              Xem bảng xếp hạng
            </button>
          </div>
        </div>
      </div>

      {/* Activity area chart + mini calendar */}
      <div className="row g-4 mt-1">
        <div className="col-md-8">
          <div className="chart-card h-100">
            <div className="chart-card-title">
              Hoạt động thông báo 7 ngày gần đây
            </div>
            <ActivityAreaChart data={activityData} />
          </div>
        </div>
        <div className="col-md-4">
          <div className="chart-card h-100">
            <div className="chart-card-title">Lịch hoạt động</div>
            <MiniCalendar highlightDates={notificationDateSet} />
          </div>
        </div>
      </div>

      {/* Notifications */}
      <div className="chart-card mt-4">
        <div className="d-flex justify-content-between align-items-center mb-2">
          <div className="chart-card-title mb-0">Thông báo gần đây</div>
          <button
            className="btn btn-sm btn-outline-primary"
            onClick={() => navigate("/mangaka/notifications")}
          >
            Xem tất cả
          </button>
        </div>
        {recentNotifications.length === 0 ? (
          <p className="empty-text">Không có thông báo.</p>
        ) : (
          <ul className="list-group">
            {recentNotifications.map((notification) => (
              <li
                key={notification.id}
                className="list-group-item notification-item"
              >
                <span>{notification.message}</span>
                {!notification.isRead && (
                  <span className="badge bg-danger ms-2">Mới</span>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
