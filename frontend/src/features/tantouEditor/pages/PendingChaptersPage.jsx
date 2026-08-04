import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { getPendingReviewChapters } from "../../../services/chapterEditorService";

export default function PendingChaptersPage() {
  const [pendingChapters, setPendingChapters] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchChapters = async () => {
      try {
        const data = await getPendingReviewChapters();
        setPendingChapters(data || []);
      } catch (error) {
        console.error("Lỗi lấy danh sách chapter:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchChapters();
  }, []);

  if (loading) {
    return (
      <div className="tantou-dashboard-loading text-center p-5">
        <div className="spinner-border text-success" role="status"></div>
        <p className="text-muted mt-3 mb-0">Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <div className="tantou-dashboard">
      <div className="dashboard-section-title mb-4">
        <i className="fas fa-book-open text-success me-2"></i>
        Chapter Cần Duyệt ({pendingChapters.length})
      </div>

      <div className="row">
        {pendingChapters.length === 0 && (
          <div className="col-12 text-center p-5 bg-white rounded shadow-sm border">
            <p className="text-muted mb-0">
              Hiện tại bạn không có Chapter nào cần kiểm duyệt.
            </p>
          </div>
        )}
        {pendingChapters.map((c) => (
          <div className="col-md-4 mb-4" key={c.id}>
            <div className="dashboard-item-card dashboard-item-card-simple h-100">
              <div className="dashboard-item-body">
                <h5 className="dashboard-item-title text-success">
                  {c.seriesTitle}
                </h5>
                <h6 className="mb-3">Chapter {c.chapterNumber}</h6>
                <button
                  className="btn btn-success w-100 mt-auto"
                  onClick={() => navigate(`/tantou/chapters/${c.id}/review`)}
                >
                  Mở &amp; Duyệt Chapter
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
