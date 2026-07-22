import React, { useState, useEffect } from "react";
import {
  getReviewingSeries,
  voteSeriesDecision,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import "../styles/EditorialBoard.css";

export default function ReviewSeriesPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [previewSeries, setPreviewSeries] = useState(null);

  useEffect(() => {
    fetchReviewingSeries();
  }, []);

  const fetchReviewingSeries = async () => {
    try {
      setLoading(true);
      const data = await getReviewingSeries();
      setSeriesList(data);
    } catch (error) {
      console.error("Lỗi khi tải danh sách chờ duyệt:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleVote = async (seriesId, title, decisionType) => {
    const actionText =
      decisionType === "APPROVE" ? "DUYỆT XUẤT BẢN" : "YÊU CẦU SỬA/TỪ CHỐI";
    try {
      await voteSeriesDecision(seriesId, decisionType);
      alert(`Đã gửi quyết định: ${actionText} thành công!`);
      fetchReviewingSeries();
    } catch (error) {
      console.error("Lỗi khi gửi quyết định:", error);
      alert(
        error.response?.data?.message || "Không thể gửi quyết định lúc này.",
      );
    }
  };

  if (loading)
    return (
      <div className="tab-content">
        <h2>Đang tải dữ liệu...</h2>
      </div>
    );

  return (
    <div className="tab-content">
      <h2 className="mb-4">📝 Xét Duyệt Tác Phẩm Mới</h2>
      <p className="text-muted mb-4">
        Hội đồng biên tập cần bỏ phiếu cho các tác phẩm đang ở trạng thái{" "}
        <strong>REVIEWING</strong>. Tác phẩm sẽ tự động xuất bản nếu đạt đủ số
        phiếu đồng thuận.
      </p>
      <div className="table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Bìa</th>
              <th>Tên Series</th>
              <th>Tác giả</th>
              <th>Hội đồng phụ trách</th>
              <th>Tiến độ bỏ phiếu</th>
              <th>Quyết định của bạn</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {seriesList.length === 0 ? (
              <tr>
                <td colSpan="8" className="text-center py-4 text-muted">
                  Hiện không có tác phẩm nào đang chờ duyệt.
                </td>
              </tr>
            ) : (
              seriesList.map((series) => (
                <tr key={series.id}>
                  <td>#{series.id}</td>
                  <td>
                    <img
                      src={
                        series.coverUrl
                          ? `http://localhost:8080/covers/${series.coverUrl}`
                          : "https://placehold.co/50x70?text=No+Cover"
                      }
                      alt={series.title}
                      style={{
                        width: "50px",
                        height: "70px",
                        objectFit: "cover",
                        borderRadius: "4px",
                      }}
                    />
                  </td>
                  <td>
                    <strong>{series.title}</strong>
                    <br />
                    <small className="text-muted">
                      {series.genres?.join(", ")}
                    </small>
                  </td>
                  <td>{series.author}</td>
                  <td>
                    {series.assignedBoardMembers?.length > 0 ? (
                      <div style={{ fontSize: "0.85rem" }}>
                        {series.assignedBoardMembers.map((member) => (
                          <div
                            key={member.boardMemberId}
                            className="mb-1"
                            style={{
                              display: "flex",
                              flexDirection: "column",
                            }}
                          >
                            <span>👤 {member.boardMemberName}</span>
                            <small className="text-muted">
                              Nhận lúc: {formatDateTime(member.assignedAt)}
                            </small>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <span className="text-muted fst-italic">
                        Chưa phân công
                      </span>
                    )}
                  </td>
                  <td>
                    <div style={{ fontSize: "0.9rem" }}>
                      <span className="text-success">
                        Duyệt: {series.approveVotes}/{series.totalBoardMembers}
                      </span>
                      <br />
                      <span className="text-danger">
                        Từ chối: {series.rejectVotes}/{series.totalBoardMembers}
                      </span>
                      <br />
                      <small className="text-muted">
                        (Cần {series.requiredVotes}/{series.totalBoardMembers}{" "}
                        phiếu đồng thuận để quyết định)
                      </small>
                    </div>
                  </td>
                  <td>
                    {series.currentUserDecision ? (
                      <span
                        className={`badge ${series.currentUserDecision === "APPROVE" ? "bg-success" : "bg-danger"}`}
                      >
                        {series.currentUserDecision}
                      </span>
                    ) : series.currentUserAssigned ? (
                      <span className="text-muted fst-italic">
                        Chưa bỏ phiếu
                      </span>
                    ) : (
                      <span className="text-muted fst-italic">
                        Không thuộc ban thẩm định này
                      </span>
                    )}
                  </td>
                  <td>
                    {series.currentUserAssigned ? (
                      <div className="d-flex gap-2">
                        <button
                          className="btn btn-outline-info btn-sm"
                          onClick={() => setPreviewSeries(series)}
                        >
                          👁 Xem hồ sơ
                        </button>
                        <button
                          className="btn-approve-sm"
                          onClick={() =>
                            handleVote(series.id, series.title, "APPROVE")
                          }
                        >
                          ✅ Duyệt
                        </button>
                        <button
                          className="btn-reject-sm"
                          onClick={() =>
                            handleVote(series.id, series.title, "REJECT")
                          }
                        >
                          ❌ Từ chối
                        </button>
                      </div>
                    ) : (
                      <span className="text-muted fst-italic">
                        Không có quyền bỏ phiếu
                      </span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {previewSeries && (
        <div
          className="custom-modal-overlay"
          onClick={() => setPreviewSeries(null)}
        >
          <div
            className="custom-modal-content series-review-preview-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="close-btn"
              onClick={() => setPreviewSeries(null)}
              aria-label="Đóng"
            >
              ✕
            </button>
            <h4 className="mb-1">{previewSeries.title}</h4>
            <p className="text-muted mb-3">
              Tác giả: <strong>{previewSeries.author}</strong>
              {previewSeries.genres?.length > 0 && (
                <> · {previewSeries.genres.join(", ")}</>
              )}
            </p>
            {previewSeries.description && (
              <p className="mb-3">{previewSeries.description}</p>
            )}
            <SeriesFileList files={previewSeries.uploadedFiles} />
          </div>
        </div>
      )}
    </div>
  );
}
