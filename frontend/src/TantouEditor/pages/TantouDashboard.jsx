import React, { useState, useEffect } from "react";
import {
  getStudioProgress,
  getPendingReviewSeries,
  getNotifications,
  acceptSeries,
  markNotificationRead,
  rejectSeries,
} from "../../services/tantouService";
import { useNavigate } from "react-router-dom";

const ASSIGNMENT_NOTIFICATION_TYPES = ["NEW_ASSIGNMENT", "SYSTEM_ASSIGNMENT"];

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

export default function TantouDashboard() {
  const [progress, setProgress] = useState([]);
  const [pendingSeries, setPendingSeries] = useState([]);
  const [pendingAssignments, setPendingAssignments] = useState([]);
  const [acceptingId, setAcceptingId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [progressData, pendingData, notificationData] = await Promise.all([
        getStudioProgress(),
        getPendingReviewSeries(),
        getNotifications(),
      ]);
      setProgress(progressData);
      setPendingSeries(pendingData);
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

  const handleReject = async (notification) => {
    const seriesId = notification.referenceId;
    if (!seriesId) return;

    const confirmed = window.confirm(
      "Bạn có chắc muốn từ chối hồ sơ này? Hệ thống sẽ tự động chuyển cho biên tập viên đang có ít việc nhất.",
    );
    if (!confirmed) return;

    setRejectingId(notification.id);
    try {
      await rejectSeries(seriesId);
      await markNotificationRead(notification.id);
      setPendingAssignments((prev) =>
        prev.filter((n) => n.id !== notification.id),
      );
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

  if (loading) return <div className="p-4">Đang tải báo cáo Studio...</div>;

  return (
    <div className="p-4 bg-light min-vh-100">
      <h2 className="mb-4">📊 Báo Cáo Tiến Độ Studio (Real-time)</h2>

      {pendingAssignments.length > 0 && (
        <div className="card shadow-sm mb-5 border-0 border-start border-4 border-warning">
          <div className="card-header bg-white fw-bold d-flex justify-content-between align-items-center">
            <span>🆕 Hồ Sơ Mới Chờ Nhận ({pendingAssignments.length})</span>
            <small className="text-muted">
              Cần bấm "Nhận hồ sơ series" trong 24h, nếu không hệ thống sẽ tự
              động chuyển cho biên tập viên khác.
            </small>
          </div>
          <div className="card-body p-0">
            <ul className="list-group list-group-flush">
              {pendingAssignments.map((n) => {
                const isAccepting = acceptingId === n.id;
                return (
                  <li
                    key={n.id}
                    className="list-group-item d-flex justify-content-between align-items-center flex-wrap gap-2"
                  >
                    <div>
                      <span className="badge bg-warning text-dark me-2">
                        Chờ xác nhận
                      </span>
                      {n.message}
                      <div>
                        <small className="text-muted">
                          {new Date(n.createdAt).toLocaleString()}
                        </small>
                      </div>
                    </div>
                    <button
                      type="button"
                      className="btn btn-sm btn-success"
                      disabled={isAccepting}
                      onClick={() => handleAccept(n)}
                    >
                      {isAccepting ? "Đang nhận..." : "Nhận hồ sơ series"}
                    </button>
                    
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-danger ms-2"
                      disabled={isAccepting || rejectingId === n.id}
                      onClick={() => handleReject(n)}
                    >
                      {rejectingId === n.id ? "Đang từ chối..." : "Từ chối"}
                    </button>
                  </li>
                );
              })}
            </ul>
          </div>
        </div>
      )}

      <div className="card shadow-sm mb-5 border-0">
        <div className="card-header bg-white fw-bold">
          Tiến độ hoàn thiện các tác phẩm đang phụ trách
        </div>
        <div className="card-body p-0">
          <table className="table table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>Tên Series</th>
                <th>Tổng số trang</th>
                <th>Trang đã hoàn thiện</th>
              </tr>
            </thead>
            <tbody>
              {progress.map((p) => (
                <tr
                  key={p.seriesId}
                  className={
                    p.status === "PENDING_EDITOR" ? "table-warning" : ""
                  }
                >
                  <td className="fw-bold">
                    {p.seriesTitle}
                    <ProgressStatusBadge status={p.status} />
                  </td>
                  <td>{p.totalPages}</td>
                  <td>{p.finalizedPages}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <h2 className="mb-4">📝 Bản Thảo Cần Kiểm Duyệt (Pending)</h2>
      <div className="row">
        {pendingSeries.map((series) => (
          <div className="col-md-4 mb-4" key={series.id}>
            <div className="card shadow-sm border-0 h-100">
              <img
                src={`http://localhost:8080/covers/${series.coverUrl}`}
                className="card-img-top"
                alt={series.title}
                style={{ height: "200px", objectFit: "cover" }}
              />
              <div className="card-body">
                <h5 className="card-title fw-bold">{series.title}</h5>
                <p className="card-text text-muted mb-1">
                  Tác giả: {series.author}
                </p>
                <p className="card-text text-muted">
                  Ngày nộp: {new Date(series.submittedAt).toLocaleDateString()}
                </p>
                <button
                  className="btn btn-primary w-100"
                  onClick={() => navigate(`/tantou/review/${series.id}`)}
                >
                  Mở Hồ Sơ & Kiểm Duyệt
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
