import React, { useState, useEffect, useMemo } from "react";
import {
  getStudioProgress,
  getPendingReviewSeries,
  getNotifications,
  acceptSeries,
  markNotificationRead,
  rejectSeries,
  getSeriesDossier,
} from "../../../services/tantouService";
import { useNavigate } from "react-router-dom";
import { getPendingReviewChapters } from "../../../services/chapterEditorService";
import { formatDateTime, formatDateOnly } from "../../../utils/formatDate";
import RejectReasonModal from "../../../components/RejectReasonModal";
import SeriesFileList from "../../../components/SeriesFileList";
import "../styles/TantouEditor.css";

const ASSIGNMENT_NOTIFICATION_TYPES = ["NEW_ASSIGNMENT", "SYSTEM_ASSIGNMENT"];

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
    <svg viewBox={`0 0 ${width} ${height}`} className="area-chart-svg">
      <defs>
        <linearGradient id="editorActivityGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stopColor="#8b5cf6" stopOpacity="0.55" />
          <stop offset="100%" stopColor="#8b5cf6" stopOpacity="0.03" />
        </linearGradient>
      </defs>
      <line
        x1={paddingX}
        y1={baselineY}
        x2={width - paddingX}
        y2={baselineY}
        className="bar-chart-axis"
      />
      <path d={area} fill="url(#editorActivityGradient)" stroke="none" />
      <path d={line} fill="none" className="area-line editor-area-line" />
      {points.map((p, i) => (
        <g key={i}>
          <circle
            cx={p.x}
            cy={p.y}
            r="3.5"
            className="area-point editor-area-point"
          />
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

  const cells = Array(startWeekday).fill(null);
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
              className={`mini-calendar-day${isToday ? " mini-calendar-today editor-today" : ""}${
                hasActivity ? " mini-calendar-active editor-active" : ""
              }`}
            >
              {day}
            </span>
          );
        })}
      </div>
      <div className="mini-calendar-legend">
        <span className="legend-dot mini-calendar-legend-dot editor-legend-dot" />{" "}
        Ngày có hoạt động
      </div>
    </div>
  );
}

export default function TantouDashboard() {
  const [progress, setProgress] = useState([]);
  const [pendingSeries, setPendingSeries] = useState([]);
  const [pendingChapters, setPendingChapters] = useState([]);
  const [pendingAssignments, setPendingAssignments] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [acceptingId, setAcceptingId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [loading, setLoading] = useState(true);

  const [previewDossier, setPreviewDossier] = useState(null);
  const [previewTargetId, setPreviewTargetId] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [progressData, pendingData, notificationData, pendingChapterData] =
        await Promise.all([
          getStudioProgress(),
          getPendingReviewSeries(),
          getNotifications(),
          getPendingReviewChapters(),
        ]);
      setProgress(progressData || []);
      setPendingSeries(pendingData || []);
      setPendingChapters(pendingChapterData || []);
      setNotifications(notificationData || []);

      const assignments = (notificationData || []).filter(
        (n) => ASSIGNMENT_NOTIFICATION_TYPES.includes(n.type) && !n.isRead,
      );
      setPendingAssignments(assignments);
    } catch (error) {
      console.error("Lỗi lấy dữ liệu Tantou:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenPreview = async (notification) => {
    try {
      setPreviewTargetId(notification.id);
      const data = await getSeriesDossier(notification.referenceId);
      setPreviewDossier({ dossier: data, notification });
    } catch (error) {
      console.error("Lỗi khi tải bản xem trước hồ sơ:", error);
      alert("Không thể tải bản xem trước hồ sơ lúc này. Vui lòng thử lại.");
    } finally {
      setPreviewTargetId(null);
    }
  };

  const handleAccept = async (notification) => {
    const seriesId = notification.referenceId;
    if (!seriesId) return;
    setAcceptingId(notification.id);
    try {
      await acceptSeries(seriesId);
      await markNotificationRead(notification.id);
      setPendingAssignments((prev) =>
        prev.filter((n) => n.id !== notification.id),
      );
      fetchDashboardData();
      navigate(`/tantou/review/${seriesId}`);
    } catch (error) {
      alert(error?.response?.data?.message || "Không thể nhận hồ sơ.");
      fetchDashboardData();
    } finally {
      setAcceptingId(null);
    }
  };

  const handleReject = (notification) => {
    if (!notification.referenceId) return;
    setRejectTarget(notification);
  };

  const handleConfirmReject = async (reason) => {
    const notification = rejectTarget;
    if (!notification) return;
    setRejectingId(notification.id);
    try {
      await rejectSeries(notification.referenceId, reason);
      await markNotificationRead(notification.id);
      setPendingAssignments((prev) =>
        prev.filter((n) => n.id !== notification.id),
      );
      setRejectTarget(null);
      fetchDashboardData();
    } catch (error) {
      alert(error?.response?.data?.message || "Không thể từ chối hồ sơ.");
      fetchDashboardData();
    } finally {
      setRejectingId(null);
    }
  };

  const avgCompletion = useMemo(() => {
    if (progress.length === 0) return 0;
    const total = progress.reduce((sum, p) => sum + (p.completionRate || 0), 0);
    return Math.round(total / progress.length);
  }, [progress]);

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
      <div className="tantou-dashboard-loading">
        <div className="spinner-border text-primary" role="status"></div>
        <p className="text-muted mt-3 mb-0">Đang đồng bộ dữ liệu Studio...</p>
      </div>
    );
  }

  return (
    <div className="tantou-dashboard">
      <div className="dashboard-page-header">
        <div>
          <h2 className="dashboard-title">Báo Cáo Tiến Độ Studio</h2>
        </div>
      </div>

      <div className="dashboard-stats-grid">
        <div className="stat-card">
          <div className="stat-icon stat-icon-blue">
            <i className="fas fa-layer-group"></i>
          </div>
          <div>
            <div className="stat-value">{progress.length}</div>
            <div className="stat-label">Series đang phụ trách</div>
          </div>
        </div>
        <div
          className="stat-card"
          style={{ cursor: "pointer" }}
          onClick={() => navigate("/tantou/pending-series")}
        >
          <div className="stat-icon stat-icon-purple">
            <i className="fas fa-file-signature"></i>
          </div>
          <div>
            <div className="stat-value">{pendingSeries.length}</div>
            <div className="stat-label">Bản thảo cần kiểm duyệt</div>
          </div>
        </div>
        <div
          className="stat-card"
          style={{ cursor: "pointer" }}
          onClick={() => navigate("/tantou/pending-chapters")}
        >
          <div className="stat-icon stat-icon-green">
            <i className="fas fa-book-open"></i>
          </div>
          <div>
            <div className="stat-value">{pendingChapters.length}</div>
            <div className="stat-label">Chapter cần duyệt</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon stat-icon-amber">
            <i className="fas fa-inbox"></i>
          </div>
          <div>
            <div className="stat-value">{pendingAssignments.length}</div>
            <div className="stat-label">Hồ sơ mới chờ nhận</div>
          </div>
        </div>
      </div>

      {pendingAssignments.length > 0 && (
        <section className="dashboard-card dashboard-card-alert mb-4">
          <div className="dashboard-card-header">
            <span>
              <i className="fas fa-bell text-warning me-2"></i>Hồ Sơ Mới Chờ
              Nhận
            </span>
            <span className="badge bg-warning text-dark">
              {pendingAssignments.length}
            </span>
          </div>
          <div className="dashboard-card-body p-0">
            <ul className="assignment-list">
              {pendingAssignments.map((n) => {
                return (
                  <li key={n.id} className="assignment-item">
                    <div className="assignment-item-main">
                      <span className="badge bg-warning text-dark mb-1">
                        Chờ xác nhận
                      </span>
                      <p className="assignment-message mb-1">{n.message}</p>
                      <small className="text-muted d-block">
                        Vui lòng xem trước hồ sơ trong 24h, nếu không hệ thống
                        sẽ tự động chuyển cho biên tập viên khác.
                      </small>
                      <small className="text-muted">
                        {formatDateTime(n.createdAt)}
                      </small>
                    </div>
                    <div className="assignment-item-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-info text-white fw-bold"
                        disabled={previewTargetId === n.id}
                        onClick={() => handleOpenPreview(n)}
                      >
                        {previewTargetId === n.id
                          ? "Đang tải hồ sơ..."
                          : "Xem hồ sơ"}
                      </button>
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>
        </section>
      )}

      <div className="row g-4 mb-4">
        <div className="col-md-8">
          <div className="dashboard-card h-100 p-4">
            <h5 className="dashboard-section-title mb-4">
              Tiến Độ Các Series (Studio Progress)
            </h5>
            {progress.length === 0 ? (
              <p className="text-muted fst-italic">
                Bạn chưa phụ trách series nào.
              </p>
            ) : (
              <div className="progress-list-container">
                {progress.map((p) => (
                  <div key={p.seriesId} className="progress-list-item mb-4">
                    <div className="d-flex justify-content-between align-items-center mb-1">
                      <span className="fw-bold">{p.seriesTitle}</span>
                      <span className="badge bg-light text-dark border">
                        {p.status}
                      </span>
                    </div>
                    <div className="d-flex justify-content-between text-muted small mb-2">
                      <span>
                        {p.finalizedPages} / {p.totalPages} trang hoàn thiện
                      </span>
                      <span className="fw-bold text-primary">
                        {Math.round(p.completionRate || 0)}%
                      </span>
                    </div>
                    <div className="progress-track">
                      <div
                        className="progress-fill"
                        style={{ width: `${p.completionRate || 0}%` }}
                      ></div>
                    </div>
                    <div className="d-flex gap-3 mt-2 small text-muted">
                      <span>
                        📝 Task mở:{" "}
                        <strong className="text-warning">
                          {p.inProgressTasks + p.assignedTasks}
                        </strong>
                      </span>
                      <span>
                        🚩 Quá hạn:{" "}
                        <strong className="text-danger">
                          {p.overdueTasks}
                        </strong>
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="col-md-4">
          <div className="dashboard-card h-100 p-4 d-flex flex-column align-items-center">
            <h5 className="dashboard-section-title mb-4 w-100">
              Hiệu Suất Tổng Thể
            </h5>
            <div className="donut-wrap my-auto">
              <div
                className="donut-ring"
                style={{
                  background: `conic-gradient(#8b5cf6 ${avgCompletion}%, #e5e7eb ${avgCompletion}% 100%)`,
                }}
              >
                <div className="donut-center">
                  <span className="donut-percent" style={{ color: "#8b5cf6" }}>
                    {avgCompletion}%
                  </span>
                  <span className="donut-caption text-center px-2">
                    Hoàn thành
                    <br />
                    trung bình
                  </span>
                </div>
              </div>
            </div>
            <p className="text-muted small mt-4 text-center">
              Trung bình tiến độ của toàn bộ {progress.length} dự án bạn đang
              giám sát.
            </p>
          </div>
        </div>
      </div>

      <div className="row g-4">
        <div className="col-md-8">
          <div className="dashboard-card h-100 p-4">
            <h5 className="dashboard-section-title mb-4">
              Lưu lượng thông báo (7 ngày qua)
            </h5>
            <ActivityAreaChart data={activityData} />
          </div>
        </div>
        <div className="col-md-4">
          <div className="dashboard-card h-100 p-4">
            <h5 className="dashboard-section-title mb-4">Lịch hoạt động</h5>
            <MiniCalendar highlightDates={notificationDateSet} />
          </div>
        </div>
      </div>

      {previewDossier && (
        <div
          className="custom-modal-overlay"
          onClick={() => setPreviewDossier(null)}
        >
          <div
            className="custom-modal-content"
            style={{
              width: "850px",
              maxWidth: "95vw",
              maxHeight: "90vh",
              overflowY: "auto",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="close-btn"
              onClick={() => setPreviewDossier(null)}
            >
              ✕
            </button>
            <h4 className="mb-4">
              Xem trước hồ sơ: {previewDossier.dossier.series.title}
            </h4>
            <div className="row mb-4">
              <div className="col-md-4">
                <img
                  src={`http://localhost:8080/covers/${previewDossier.dossier.series.coverUrl}`}
                  alt={previewDossier.dossier.series.title}
                  className="img-fluid rounded shadow-sm"
                  onError={(e) =>
                    (e.target.src =
                      "https://placehold.co/250x350?text=No+Cover")
                  }
                />
              </div>
              <div className="col-md-8">
                <p>
                  <strong>👤 Tác giả:</strong>{" "}
                  {previewDossier.dossier.series.author}
                </p>
                <p>
                  <strong>🏷️ Thể loại:</strong>{" "}
                  {previewDossier.dossier.series.genres?.join(", ") || "N/A"}
                </p>
                <p>
                  <strong>📖 Mô tả:</strong>{" "}
                  {previewDossier.dossier.series.description ||
                    "Chưa có mô tả."}
                </p>
              </div>
            </div>

            <h6 className="fw-bold mb-3 border-bottom pb-2">
              Bản thảo & Tài liệu đính kèm
            </h6>
            <div className="mb-4 bg-light p-3 rounded border">
              <SeriesFileList
                files={previewDossier.dossier.series.uploadedFiles || []}
                emptyText="Mangaka chưa tải lên bản thảo nào."
              />
            </div>

            <div className="d-flex justify-content-end gap-3 border-top pt-3">
              <button
                className="btn btn-secondary"
                onClick={() => setPreviewDossier(null)}
              >
                Đóng
              </button>
              <button
                className="btn btn-outline-danger px-4"
                disabled={acceptingId === previewDossier.notification.id}
                onClick={() => {
                  handleReject(previewDossier.notification);
                  setPreviewDossier(null);
                }}
              >
                Từ chối
              </button>
              <button
                className="btn btn-success px-4"
                disabled={acceptingId === previewDossier.notification.id}
                onClick={() => {
                  handleAccept(previewDossier.notification);
                  setPreviewDossier(null);
                }}
              >
                {acceptingId === previewDossier.notification.id
                  ? "Đang xử lý..."
                  : "✅ Chấp nhận phụ trách"}
              </button>
            </div>
          </div>
        </div>
      )}

      {rejectTarget && (
        <RejectReasonModal
          seriesTitle={rejectTarget.message}
          submitting={rejectingId === rejectTarget.id}
          onCancel={() => setRejectTarget(null)}
          onConfirm={handleConfirmReject}
        />
      )}
    </div>
  );
}
