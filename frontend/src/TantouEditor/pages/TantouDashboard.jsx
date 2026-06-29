import React, { useState, useEffect } from "react";
import {
  getStudioProgress,
  getPendingReviewSeries,
} from "../../services/tantouService";
import { useNavigate } from "react-router-dom";

export default function TantouDashboard() {
  const [progress, setProgress] = useState([]);
  const [pendingSeries, setPendingSeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const [progressData, pendingData] = await Promise.all([
        getStudioProgress(),
        getPendingReviewSeries(),
      ]);
      setProgress(progressData);
      setPendingSeries(pendingData);
    } catch (error) {
      console.error("Lỗi lấy dữ liệu Tantou:", error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="p-4">Đang tải báo cáo Studio...</div>;

  return (
    <div className="p-4 bg-light min-vh-100">
      <h2 className="mb-4">📊 Báo Cáo Tiến Độ Studio (Real-time)</h2>

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
                <th>Tỷ lệ hoàn thành</th>
                <th>Task trễ hạn (Overdue)</th>
                <th>Deadline tiếp theo</th>
              </tr>
            </thead>
            <tbody>
              {progress.map((p) => (
                <tr key={p.seriesId}>
                  <td className="fw-bold">{p.seriesTitle}</td>
                  <td>{p.totalPages}</td>
                  <td>{p.finalizedPages}</td>
                  <td>
                    <div className="progress" style={{ height: "20px" }}>
                      <div
                        className="progress-bar bg-success"
                        style={{ width: `${p.completionRate}%` }}
                      >
                        {p.completionRate}%
                      </div>
                    </div>
                  </td>
                  <td>
                    {p.overdueTasks > 0 ? (
                      <span className="badge bg-danger">
                        {p.overdueTasks} task trễ
                      </span>
                    ) : (
                      <span className="badge bg-success">Đúng tiến độ</span>
                    )}
                  </td>
                  <td>
                    {p.nextDeadline
                      ? new Date(p.nextDeadline).toLocaleDateString()
                      : "Chưa có"}
                  </td>
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
