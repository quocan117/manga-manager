import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getMyTasks,
  getTaskSubmissions,
} from "../../../services/assistantService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import "../../../styles/SeriesModal.css"; 

export default function MySubmissions() {
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [previewItem, setPreviewItem] = useState(null); 
  const navigate = useNavigate();

  useEffect(() => {
    loadSubmissions();
  }, []);

  const loadSubmissions = async () => {
    try {
      const tasks = await getMyTasks();
      const relevantTasks = tasks.filter((t) =>
        ["SUBMITTED", "APPROVED", "REVISION_REQUESTED"].includes(t.status),
      );

      const results = await Promise.all(
        relevantTasks.map((t) => getTaskSubmissions(t.id)),
      );

      let allSubmissions = [];
      relevantTasks.forEach((task, index) => {
        const taskSubs = results[index] || [];
        taskSubs.forEach((sub) => {
          allSubmissions.push({
            ...sub,
            seriesTitle: task.seriesTitle,
            chapterNumber: task.chapterNumber,
            pageNumber: task.pageNumber,
            taskTitle: task.title,
          });
        });
      });

      allSubmissions.sort((a, b) => {
        return new Date(b.submittedAt) - new Date(a.submittedAt);
      });

      setSubmissions(allSubmissions);
    } catch (error) {
      console.error("Lỗi khi tải submissions:", error);
    } finally {
      setLoading(false);
    }
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case "APPROVED":
        return <span className="badge bg-success px-3 py-2">Đã duyệt</span>;
      case "REVISION_REQUESTED":
        return (
          <span className="badge bg-danger px-3 py-2">Yêu cầu sửa lại</span>
        );
      case "SUBMITTED":
        return <span className="badge bg-primary px-3 py-2">Đã nộp</span>;
      default:
        return <span className="badge bg-secondary px-3 py-2">{status}</span>;
    }
  };

  if (loading)
    return (
      <div className="text-center mt-5">
        <div className="spinner-border text-primary" role="status"></div>
        <p className="mt-3 text-muted">Đang tải dữ liệu bài nộp...</p>
      </div>
    );

  return (
    <div className="container-fluid px-0">
      <h2 className="mb-4 fw-bold text-dark">Bài nộp của tôi</h2>

      <div className="card shadow-sm border-0">
        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th
                    className="ps-4 py-3 text-uppercase text-secondary"
                    style={{ fontSize: "0.85rem", width: "8%" }}
                  >
                    ID
                  </th>
                  <th
                    className="py-3 text-uppercase text-secondary"
                    style={{ fontSize: "0.85rem", width: "25%" }}
                  >
                    Thông tin Task
                  </th>
                  <th
                    className="py-3 text-uppercase text-secondary"
                    style={{ fontSize: "0.85rem", width: "15%" }}
                  >
                    Tình trạng
                  </th>
                  <th
                    className="py-3 text-uppercase text-secondary"
                    style={{ fontSize: "0.85rem", width: "25%" }}
                  >
                    Ghi chú của Mangaka
                  </th>
                  <th
                    className="py-3 text-uppercase text-secondary"
                    style={{ fontSize: "0.85rem", width: "15%" }}
                  >
                    Thời gian nộp
                  </th>
                  <th
                    className="pe-4 py-3 text-end text-uppercase text-secondary"
                    style={{ fontSize: "0.85rem" }}
                  >
                    Hành động
                  </th>
                </tr>
              </thead>
              <tbody>
                {submissions.length === 0 && (
                  <tr>
                    <td
                      colSpan={6}
                      className="text-center py-5 text-muted fst-italic"
                    >
                      <div className="display-4 opacity-25 mb-3">📁</div>
                      Bạn chưa có bài nộp nào.
                    </td>
                  </tr>
                )}
                {submissions.map((item) => (
                  <tr key={item.id}>
                    <td className="ps-4 fw-bold text-secondary">#{item.id}</td>
                    <td>
                      <div className="fw-bold text-dark mb-1">
                        {item.taskTitle || `Task #${item.taskId}`}
                      </div>
                      <div className="small text-muted">
                        <i className="fas fa-book me-1"></i>
                        {item.seriesTitle} · Ch.{item.chapterNumber} · Trang{" "}
                        {item.pageNumber}
                      </div>
                    </td>
                    <td>{renderStatusBadge(item.status)}</td>
                    <td>
                      {item.reviewNote ? (
                        <span
                          className={
                            item.status === "REVISION_REQUESTED"
                              ? "text-danger fst-italic fw-bold"
                              : "text-dark"
                          }
                        >
                          {item.reviewNote}
                        </span>
                      ) : (
                        <span className="text-muted fst-italic">
                          - Không có -
                        </span>
                      )}
                    </td>
                    <td className="text-muted small">
                      {item.submittedAt
                        ? formatDateTime(item.submittedAt)
                        : "-"}
                    </td>
                    <td className="pe-4 text-end">
                      <button
                        className="btn btn-sm btn-outline-info fw-bold px-3 shadow-sm"
                        onClick={() => setPreviewItem(item)}
                      >
                        <i className="fas fa-image me-1"></i> Xem trang truyện
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {previewItem && (
        <div
          className="custom-modal-overlay"
          onClick={() => setPreviewItem(null)}
        >
          <div
            className="custom-modal-content"
            style={{
              width: "800px",
              maxWidth: "95vw",
              maxHeight: "90vh",
              overflowY: "auto",
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <button className="close-btn" onClick={() => setPreviewItem(null)}>
              ✕
            </button>
            <h4 className="mb-2 text-primary">
              Chi tiết bài nộp:{" "}
              {previewItem.taskTitle || `Task #${previewItem.taskId}`}
            </h4>
            <p className="text-muted mb-4">
              <i className="fas fa-book me-1"></i>
              {previewItem.seriesTitle} · Chapter {previewItem.chapterNumber} ·
              Trang {previewItem.pageNumber}
            </p>

            <h6 className="fw-bold mb-3 border-bottom pb-2">
              <i className="fas fa-file-image me-2"></i>Tài liệu đã nộp
            </h6>
            <div className="bg-light p-3 rounded border mb-4">
              <SeriesFileList
                files={previewItem.resultFiles || []}
                emptyText="Không có file nào được đính kèm."
              />
            </div>

            {previewItem.reviewNote && (
              <>
                <h6 className="fw-bold mb-3 border-bottom pb-2 text-danger">
                  <i className="fas fa-comment-dots me-2"></i>Phản hồi từ
                  Mangaka
                </h6>
                <div className="p-3 bg-danger bg-opacity-10 border border-danger rounded text-danger fst-italic lh-base">
                  "{previewItem.reviewNote}"
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
