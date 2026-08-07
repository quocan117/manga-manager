import React, { useState, useEffect } from "react";
import {
  getReviewingSeries,
  voteSeriesDecision,
} from "../../../services/boardService";
import api from "../../../services/api";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import RejectReasonModal from "../../../components/RejectReasonModal";
import "../styles/EditorialBoard.css";

export default function ReviewSeriesPage() {
  const [activeTab, setActiveTab] = useState("pending");
  const [seriesList, setSeriesList] = useState([]);
  const [historyList, setHistoryList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [previewSeries, setPreviewSeries] = useState(null);
  const [rejectTarget, setRejectTarget] = useState(null);
  const [submittingVote, setSubmittingVote] = useState(false);

  useEffect(() => {
    if (activeTab === "pending") {
      fetchReviewingSeries();
    } else {
      fetchReviewedSeries();
    }
  }, [activeTab]);

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

  const fetchReviewedSeries = async () => {
    try {
      setLoading(true);
      const response = await api.get("/editorial-board/series/reviewed");
      setHistoryList(response.data || []);
    } catch (error) {
      console.error("Lỗi khi tải lịch sử đã duyệt:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleVote = async (seriesId, title, decisionType, reason = "") => {
    const actionText = decisionType === "APPROVE" ? "DUYỆT" : "TỪ CHỐI";
    try {
      setSubmittingVote(true);
      await voteSeriesDecision(seriesId, decisionType, reason);
      alert(`Đã gửi quyết định: ${actionText} thành công!`);
      setRejectTarget(null);
      fetchReviewingSeries();
    } catch (error) {
      console.error("Lỗi khi gửi quyết định:", error);
      alert(
        error.response?.data?.message || "Không thể gửi quyết định lúc này.",
      );
    } finally {
      setSubmittingVote(false);
    }
  };

  const currentData = activeTab === "pending" ? seriesList : historyList;

  return (
    <div className="tab-content">
      <h2 className="mb-4">📝 Xét Duyệt Tác Phẩm Mới</h2>
      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button
            className={`nav-link fw-bold ${activeTab === "pending" ? "active text-primary" : "text-secondary"}`}
            onClick={() => setActiveTab("pending")}
            style={{
              borderBottom:
                activeTab === "pending" ? "3px solid #3b82f6" : "none",
            }}
          >
            <i className="fas fa-hourglass-half me-2"></i> Chờ xét duyệt
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link fw-bold ${activeTab === "history" ? "active text-primary" : "text-secondary"}`}
            onClick={() => setActiveTab("history")}
            style={{
              borderBottom:
                activeTab === "history" ? "3px solid #3b82f6" : "none",
            }}
          >
            <i className="fas fa-history me-2"></i> Lịch sử đã duyệt
          </button>
        </li>
      </ul>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="mt-3 text-muted">Đang tải dữ liệu...</p>
        </div>
      ) : (
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
              {currentData.length === 0 ? (
                <tr>
                  <td
                    colSpan="8"
                    className="text-center py-5 text-muted fst-italic"
                  >
                    {activeTab === "pending"
                      ? "Hiện không có tác phẩm nào đang chờ duyệt."
                      : "Chưa có tác phẩm nào hoàn tất xét duyệt."}
                  </td>
                </tr>
              ) : (
                currentData.map((series) => (
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
                      {activeTab === "history" && (
                        <span className="badge bg-secondary d-inline-block mt-1 w-50">
                          {series.status}
                        </span>
                      )}
                      <small className="text-muted d-block mt-1">
                        {series.genres?.join(", ")}
                      </small>
                    </td>
                    <td>{series.author}</td>
                    <td>
                      {series.assignedBoardMembers?.length > 0 ? (
                        <div style={{ fontSize: "0.85rem" }}>
                          {series.assignedBoardMembers.map((member) => {
                            const decision = series.decisions?.find(
                              (d) => d.boardMemberId === member.boardMemberId,
                            );
                            return (
                              <div
                                key={member.boardMemberId}
                                className="mb-2 p-1 border-bottom"
                                style={{
                                  display: "flex",
                                  flexDirection: "column",
                                }}
                              >
                                <span>
                                  👤 {member.boardMemberName}{" "}
                                  {decision ? (
                                    <span
                                      className={`badge ${
                                        decision.decisionType === "APPROVE"
                                          ? "bg-success"
                                          : "bg-danger"
                                      }`}
                                    >
                                      {decision.decisionType === "APPROVE"
                                        ? "Đã duyệt"
                                        : "Đã từ chối"}
                                    </span>
                                  ) : (
                                    <span className="badge bg-secondary">
                                      Chưa bỏ phiếu
                                    </span>
                                  )}
                                </span>
                                {decision?.decisionType === "REJECT" &&
                                  decision.reason && (
                                    <small className="text-danger mt-1 fst-italic">
                                      Lý do: {decision.reason}
                                    </small>
                                  )}
                              </div>
                            );
                          })}
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
                          Duyệt: {series.approveVotes}/
                          {series.totalBoardMembers}
                        </span>
                        <br />
                        <span className="text-danger">
                          Từ chối: {series.rejectVotes}/
                          {series.totalBoardMembers}
                        </span>
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
                      <div className="d-flex flex-wrap gap-2">
                        <button
                          className="btn btn-outline-info btn-sm fw-bold shadow-sm"
                          onClick={() => setPreviewSeries(series)}
                        >
                          <i className="fas fa-eye me-1"></i> Hồ sơ
                        </button>
                        {activeTab === "pending" &&
                          series.currentUserAssigned && (
                            <>
                              <button
                                className="btn-approve-sm shadow-sm"
                                onClick={() =>
                                  handleVote(series.id, series.title, "APPROVE")
                                }
                                disabled={submittingVote}
                              >
                                ✅ Duyệt
                              </button>
                              <button
                                className="btn-reject-sm shadow-sm"
                                onClick={() => setRejectTarget(series)}
                                disabled={submittingVote}
                              >
                                ❌ Từ chối
                              </button>
                            </>
                          )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {previewSeries && (
        <div
          className="custom-modal-overlay"
          onClick={() => setPreviewSeries(null)}
        >
          <div
            className="custom-modal-content series-review-preview-modal"
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
              onClick={() => setPreviewSeries(null)}
              aria-label="Đóng"
            >
              ✕
            </button>
            <h4 className="mb-1 text-primary">{previewSeries.title}</h4>
            <p className="text-muted mb-3">
              Tác giả: <strong>{previewSeries.author}</strong>
              {previewSeries.genres?.length > 0 && (
                <> · {previewSeries.genres.join(", ")}</>
              )}
            </p>

            {previewSeries.description && (
              <p className="mb-3 bg-light p-3 rounded">
                {previewSeries.description}
              </p>
            )}

            {(() => {
              const editorNote = previewSeries.reviewHistory?.find(
                (h) => h.action && h.action.includes("SUBMITTED_TO_BOARD"),
              )?.reason;

              return (
                <>
                  <h6 className="fw-bold mb-2 mt-4 text-uppercase text-primary border-bottom pb-2">
                    <i className="fas fa-comment-dots me-2"></i>Báo cáo từ Biên
                    tập viên
                  </h6>
                  {editorNote ? (
                    <p className="mb-3 bg-info bg-opacity-10 p-3 rounded border border-info fst-italic lh-base">
                      "{editorNote}"
                    </p>
                  ) : (
                    <p className="text-muted fst-italic mb-3">
                      Không có báo cáo đính kèm.
                    </p>
                  )}
                </>
              );
            })()}

            <SeriesFileList files={previewSeries.uploadedFiles} />
          </div>
        </div>
      )}

      {rejectTarget && (
        <RejectReasonModal
          seriesTitle={rejectTarget.title}
          submitting={submittingVote}
          onCancel={() => setRejectTarget(null)}
          onConfirm={(reason) =>
            handleVote(rejectTarget.id, rejectTarget.title, "REJECT", reason)
          }
        />
      )}
    </div>
  );
}
