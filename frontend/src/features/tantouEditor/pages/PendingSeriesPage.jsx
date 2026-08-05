import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getPendingReviewSeries } from "../../../services/tantouService";
import { formatDateOnly } from "../../../utils/formatDate";

export default function PendingSeriesPage() {
  const [pendingSeries, setPendingSeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchSeries = async () => {
      try {
        const data = await getPendingReviewSeries();
        setPendingSeries(data || []);
      } catch (error) {
        console.error("Lỗi lấy danh sách bản thảo:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchSeries();
  }, []);

  if (loading) {
    return (
      <div className="tantou-dashboard-loading text-center p-5">
        <div className="spinner-border text-primary" role="status"></div>
        <p className="text-muted mt-3 mb-0">Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <div className="tantou-dashboard">
      <div className="dashboard-section-title mb-4">
        <i className="fas fa-file-signature text-primary me-2"></i>
        Bản Thảo Cần Kiểm Duyệt ({pendingSeries.length})
      </div>

      <div className="row">
        {pendingSeries.length === 0 && (
          <div className="col-12 text-center p-5 bg-white rounded shadow-sm border">
            <p className="text-muted mb-0">
              Hiện tại bạn không có bản thảo nào cần kiểm duyệt.
            </p>
          </div>
        )}
        {pendingSeries.map((series) => (
          <div className="col-md-4 mb-4" key={series.id}>
            <div className="dashboard-item-card h-100">
              <div className="dashboard-item-cover">
                <img
                  src={`http://localhost:8080/covers/${series.coverUrl}`}
                  alt={series.title}
                  onError={(e) => {
                    e.target.src = "https://placehold.co/250x350?text=No+Cover";
                  }}
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
                  className="btn btn-primary w-100 mt-auto"
                  onClick={() => navigate(`/tantou/review/${series.id}`)}
                >
                  Mở Hồ Sơ &amp; Kiểm Duyệt
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
