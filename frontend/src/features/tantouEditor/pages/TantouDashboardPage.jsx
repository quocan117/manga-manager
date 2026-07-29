import React, { useState, useEffect } from "react";
import {
  getStudioProgress,
  getPendingReviewSeries,
  getNotifications,
  acceptSeries,
  markNotificationRead,
  rejectSeries,
} from "../../../services/tantouService";
import { useNavigate } from "react-router-dom";
import { getPendingReviewChapters } from "../../../services/chapterEditorService";
import { formatDateTime, formatDateOnly } from "../../../utils/formatDate";
import RejectReasonModal from "../../../components/RejectReasonModal";

const ASSIGNMENT_NOTIFICATION_TYPES = [
  "NEW_ASSIGNMENT",
  "SYSTEM_ASSIGNMENT",
  "FORCED_EDITOR_ASSIGNMENT",
];
const LOCKED_ASSIGNMENT_TYPES = ["FORCED_EDITOR_ASSIGNMENT"];
const PROGRESS_STATUS_LABELS = {
  PENDING_EDITOR: "Chờ xác nhận",
  TANTOU_REVIEW: "Đang kiểm tra",
  REVIEWING: "Hội đồng đang xét duyệt",
  REVISION_REQUESTED: "Yêu cầu chỉnh sửa",
  Published: "Đã xuất bản",
  PUBLISHED: "Đã xuất bản",
};

function ProgressStatusBadge({ status }) {
  if (!status) return null;
  const label = PROGRESS_STATUS_LABELS[status] || status;
  const isPending = status === "PENDING_EDITOR";
  return (
    <span
      className={`badge ms-2 ${isPending ? "bg-warning text-dark" : "bg-light text-dark border"}`}
    >
      {label}
    </span>
  );
}

function getProgressPercent(finalizedPages, totalPages) {
  if (!totalPages || totalPages <= 0) return 0;
  const percent = (finalizedPages / totalPages) * 100;
  return Math.max(0, Math.min(100, Math.round(percent)));
}

export default function TantouDashboard() {
  const [progress, setProgress] = useState([]);
  const [pendingSeries, setPendingSeries] = useState([]);
  const [pendingChapters, setPendingChapters] = useState([]);
  const [pendingAssignments, setPendingAssignments] = useState([]);
  const [acceptingId, setAcceptingId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [loading, setLoading] = useState(true);
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
      setProgress(progressData);
      setPendingSeries(pendingData);
      setPendingChapters(pendingChapterData);
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
      console.error("Lỗi nhận hồ sơ:", error);
      alert(
        error?.response?.data?.message ||
          "Không thể nhận hồ sơ. Có thể hồ sơ đã bị thu hồi hoặc biên tập khác đã nhận trước.",
      );
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
    const seriesId = notification.referenceId;
    setRejectingId(notification.id);
    try {
      await rejectSeries(seriesId, reason);
      await markNotificationRead(notification.id);
      setPendingAssignments((prev) =>
        prev.filter((n) => n.id !== notification.id),
      );
      setRejectTarget(null);
      fetchDashboardData();
    } catch (error) {
      console.error("Lỗi từ chối hồ sơ:", error);
      alert(
        error?.response?.data?.message ||
          "Không thể từ chối hồ sơ. Vui lòng thử lại.",
      );
      fetchDashboardData();
    } finally {
      setRejectingId(null);
    }
  };

  if (loading) {
    return (
      <div className="tantou-dashboard-loading">
        <div className="spinner-border text-success" role="status">
          <span className="visually-hidden">Đang tải...</span>
        </div>
        <p className="text-muted mt-3 mb-0">Đang tải báo cáo Studio...</p>
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
        <div className="stat-card">
          <div className="stat-icon stat-icon-purple">
            <i className="fas fa-file-signature"></i>
          </div>
          <div>
            <div className="stat-value">{pendingSeries.length}</div>
            <div className="stat-label">Bản thảo cần kiểm duyệt</div>
          </div>
        </div>
        <div className="stat-card">
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
              <i className="fas fa-bell text-warning me-2"></i>
              Hồ Sơ Mới Chờ Nhận
            </span>
            <span className="badge bg-warning text-dark">
              {pendingAssignments.length}
            </span>
          </div>
          <div className="dashboard-card-body p-0">
            <ul className="assignment-list">
              {pendingAssignments.map((n) => {
                const isAccepting = acceptingId === n.id;
                const isLocked = LOCKED_ASSIGNMENT_TYPES.includes(n.type);
                return (
                  <li key={n.id} className="assignment-item">
                    <div className="assignment-item-main">
                      <span className="badge bg-warning text-dark mb-1">
                        Chờ xác nhận
                      </span>
                      <p className="assignment-message mb-1">{n.message}</p>
                      <small className="text-muted d-block">
                        {isLocked
                          ? "Hồ sơ do Hội đồng Biên tập chỉ định trực tiếp — không thể từ chối."
                          : 'Cần bấm "Nhận hồ sơ series" trong 24h, nếu không hệ thống sẽ tự động chuyển cho biên tập viên khác.'}
                      </small>
                      <small className="text-muted">
                        {formatDateTime(n.createdAt)}
                      </small>
                    </div>
                    <div className="assignment-item-actions">
                      <button
                        type="button"
                        className="btn btn-sm btn-success"
                        disabled={isAccepting}
                        onClick={() => handleAccept(n)}
                      >
                        {isAccepting ? "Đang nhận..." : "Nhận hồ sơ series"}
                      </button>
                      {!isLocked && (
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-danger"
                          disabled={isAccepting || rejectingId === n.id}
                          onClick={() => handleReject(n)}
                        >
                          {rejectingId === n.id ? "Đang từ chối..." : "Từ chối"}
                        </button>
                      )}
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>
        </section>
      )}

      <div className="dashboard-section-title">
        <i className="fas fa-file-signature text-primary me-2"></i>
        Bản Thảo Cần Kiểm Duyệt
      </div>
      <div className="row">
        {pendingSeries.length === 0 && (
          <div className="col-12">
            <p className="text-muted">Chưa có bản thảo nào cần kiểm duyệt.</p>
          </div>
        )}
        {pendingSeries.map((series) => (
          <div className="col-md-4 mb-4" key={series.id}>
            <div className="dashboard-item-card h-100">
              <div className="dashboard-item-cover">
                <img
                  src={`http://localhost:8080/covers/${series.coverUrl}`}
                  alt={series.title}
                />
              </div>
              <div className="dashboard-item-body">
                <h5 className="dashboard-item-title">{series.title}</h5>
                <p className="dashboard-item-meta mb-1">
                  <i className="fas fa-user me-1"></i>
                  Tác giả: {series.author}
                </p>
                <p className="dashboard-item-meta">
                  <i className="fas fa-calendar-alt me-1"></i>
                  Ngày nộp: {formatDateOnly(series.submittedAt)}
                </p>
                <button
                  className="btn btn-primary w-100"
                  onClick={() => navigate(`/tantou/review/${series.id}`)}
                >
                  Mở Hồ Sơ &amp; Kiểm Duyệt
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="dashboard-section-title mt-4">
        <i className="fas fa-book-open text-success me-2"></i>
        Chapter Cần Duyệt
      </div>
      <div className="row">
        {pendingChapters.length === 0 && (
          <div className="col-12">
            <p className="text-muted">Chưa có chapter nào cần duyệt.</p>
          </div>
        )}
        {pendingChapters.map((c) => (
          <div className="col-md-4 mb-4" key={c.id}>
            <div className="dashboard-item-card dashboard-item-card-simple h-100">
              <div className="dashboard-item-body">
                <h5 className="dashboard-item-title">
                  {c.seriesTitle} - Chapter {c.chapterNumber}
                </h5>
                <button
                  className="btn btn-primary w-100 mt-2"
                  onClick={() => navigate(`/tantou/chapters/${c.id}/review`)}
                >
                  Mở &amp; Duyệt Chapter
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

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
