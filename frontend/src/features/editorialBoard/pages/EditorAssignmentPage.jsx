import React, { useState, useEffect } from "react";
import {
  getEditorAssignmentRequiredSeries,
  assignEditor,
  getUsers,
  cancelSeries,
  getAssignmentHistory,
  getSeriesReview,
  getDropRequestedSeries,
  voteSeriesDecision,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import RejectReasonModal from "../../../components/RejectReasonModal";
import { useAuth } from "../../../context/AuthContext";
import "../styles/EditorialBoard.css";

export default function EditorAssignmentPage() {
  const { user } = useAuth();
  const isRepresentative = user?.email === "editorial1@manga.test";

  const [activeTab, setActiveTab] = useState("pending");

  const [pendingList, setPendingList] = useState([]);
  const [dropList, setDropList] = useState([]);
  const [historyList, setHistoryList] = useState([]);
  const [editors, setEditors] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selectedEditor, setSelectedEditor] = useState({});
  const [assigningId, setAssigningId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);

  const [actionTarget, setActionTarget] = useState(null);
  const [actionType, setActionType] = useState("");
  const [submittingVote, setSubmittingVote] = useState(false);

  const [previewSeries, setPreviewSeries] = useState(null);

  useEffect(() => {
    if (activeTab === "pending") {
      fetchPendingData();
    } else if (activeTab === "drop_requested") {
      fetchDropData();
    } else if (activeTab === "history" && isRepresentative) {
      fetchHistoryData();
    }
  }, [activeTab]);

  const fetchPendingData = async () => {
    try {
      setLoading(true);
      const [seriesData, usersData] = await Promise.all([
        getEditorAssignmentRequiredSeries(),
        getUsers(),
      ]);
      setPendingList(seriesData || []);
      setEditors(
        (usersData || []).filter(
          (u) => u.role === "TANTOU_EDITOR" && u.status === "ACTIVE",
        ),
      );
    } catch (error) {
      console.error("Lỗi tải danh sách cần phân công:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchDropData = async () => {
    try {
      setLoading(true);
      const dropData = await getDropRequestedSeries();
      setDropList(dropData || []);
    } catch (error) {
      console.error("Lỗi tải danh sách yêu cầu hủy:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchHistoryData = async () => {
    try {
      setLoading(true);
      const data = await getAssignmentHistory();
      setHistoryList(data || []);
    } catch (error) {
      console.error("Lỗi tải lịch sử phân công:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectEditor = (seriesId, editorId) => {
    if (!isRepresentative) return;
    setSelectedEditor((prev) => ({ ...prev, [seriesId]: editorId }));
  };

  const handleAssign = async (seriesId) => {
    if (!isRepresentative) return;
    const editorId = selectedEditor[seriesId];
    if (!editorId) {
      alert(
        "Vui lòng chọn một Biên tập viên từ bảng bên dưới trước khi phân công.",
      );
      return;
    }
    setAssigningId(seriesId);
    try {
      await assignEditor(seriesId, Number(editorId));
      alert("Đã phân công Biên tập viên thành công!");
      setPendingList((prev) => prev.filter((s) => s.id !== seriesId));
    } catch (error) {
      alert(
        error?.response?.data?.message ||
          "Không thể phân công biên tập viên lúc này.",
      );
    } finally {
      setAssigningId(null);
    }
  };

  const handleRejectFinal = async (seriesId) => {
    if (!isRepresentative) return;
    const reason = window.prompt(
      "Nhập lý do đánh rớt tác phẩm này (sẽ gửi thông báo cho Mangaka):",
      "Hội đồng từ chối do không tìm được Biên tập viên phù hợp chuyên môn.",
    );
    if (reason === null) return;
    setRejectingId(seriesId);
    try {
      await cancelSeries(seriesId, reason);
      alert("Đã đánh rớt tác phẩm thành công!");
      setPendingList((prev) => prev.filter((s) => s.id !== seriesId));
    } catch (error) {
      alert(
        error?.response?.data?.message || "Không thể từ chối tác phẩm lúc này.",
      );
    } finally {
      setRejectingId(null);
    }
  };

  const triggerDropAction = (series, type) => {
    setActionTarget(series);
    setActionType(type);
  };

  const handleVoteDrop = async (seriesId, decisionType, reason = "") => {
    try {
      setSubmittingVote(true);
      await voteSeriesDecision(seriesId, decisionType, reason);
      alert("Đã xử lý yêu cầu hủy thành công!");
      setActionTarget(null);
      fetchDropData();
    } catch (error) {
      console.error("Lỗi khi gửi quyết định:", error);
      alert(
        error.response?.data?.message || "Không thể gửi quyết định lúc này.",
      );
    } finally {
      setSubmittingVote(false);
    }
  };

  const handleViewHistoryDossier = async (seriesId) => {
    try {
      const data = await getSeriesReview(seriesId);
      setPreviewSeries(data);
    } catch (error) {
      console.error("Lỗi lấy chi tiết hồ sơ:", error);
      alert(
        "Không thể tải hồ sơ. Có thể tác phẩm đã bị xóa hoặc bạn không có quyền xem.",
      );
    }
  };

  return (
    <div className="tab-content">
      <h2 className="mb-4">🧭 Quản Lý Phân Công & Điều Phối</h2>

      {!isRepresentative && (
        <div className="alert alert-warning shadow-sm border-0 mb-4 d-flex align-items-center">
          <i className="fas fa-exclamation-triangle fs-4 me-3 text-warning"></i>
          <div>
            <strong>Chế độ xem (View-only):</strong> Chỉ tài khoản{" "}
            <strong>Trưởng ban (Hội đồng 1)</strong> mới có quyền thao tác phân
            công hoặc xử lý tại đây.
          </div>
        </div>
      )}

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
            <i className="fas fa-hourglass-half me-2"></i> Cần phân công{" "}
            {activeTab === "pending" && `(${pendingList.length})`}
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link fw-bold ${activeTab === "drop_requested" ? "active text-danger" : "text-secondary"}`}
            onClick={() => setActiveTab("drop_requested")}
            style={{
              borderBottom:
                activeTab === "drop_requested" ? "3px solid #dc3545" : "none",
            }}
          >
            <i className="fas fa-exclamation-circle me-2"></i> BTV Yêu cầu hủy{" "}
            {activeTab === "drop_requested" && `(${dropList.length})`}
          </button>
        </li>
        {isRepresentative && (
          <li className="nav-item">
            <button
              className={`nav-link fw-bold ${activeTab === "history" ? "active text-primary" : "text-secondary"}`}
              onClick={() => setActiveTab("history")}
              style={{
                borderBottom:
                  activeTab === "history" ? "3px solid #3b82f6" : "none",
              }}
            >
              <i className="fas fa-history me-2"></i> Lịch sử điều phối
            </button>
          </li>
        )}
      </ul>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="mt-3 text-muted">Đang tải dữ liệu...</p>
        </div>
      ) : activeTab === "pending" ? (
        <>
          <p className="text-muted mb-3">
            Danh sách các tác phẩm bị BTV từ chối nhận. Trưởng ban cần chỉ định
            BTV khác hoặc đình bản tác phẩm.
          </p>
          {pendingList.length === 0 ? (
            <div className="alert alert-success border-0 shadow-sm py-4 text-center mt-4">
              <i className="fas fa-check-circle fs-3 text-success mb-2 d-block"></i>
              Hiện không có hồ sơ nào bị kẹt phân công.
            </div>
          ) : (
            pendingList.map((series) => (
              <div key={series.id} className="card shadow-sm border-0 mb-5">
                <div className="card-header bg-light border-bottom fw-bold d-flex justify-content-between align-items-center py-3">
                  <span className="fs-5 text-primary">
                    #{series.id} — {series.title}{" "}
                    <span className="text-muted fs-6">({series.author})</span>
                  </span>
                  <button
                    className="btn btn-outline-primary btn-sm fw-bold shadow-sm"
                    onClick={() => setPreviewSeries(series)}
                  >
                    <i className="fas fa-eye me-1"></i> Xem hồ sơ
                  </button>
                </div>
                <div className="card-body">
                  <h6 className="mb-3 fw-bold text-secondary">
                    <i className="fas fa-history me-1"></i> Lịch sử BTV từ chối
                    ({series.rejectedEditors?.length || 0} lần)
                  </h6>
                  <div className="table-wrapper mb-4">
                    <table className="admin-table table-sm">
                      <thead className="table-light">
                        <tr>
                          <th>Họ tên BTV</th>
                          <th>Lý do từ chối</th>
                          <th>Thời gian</th>
                        </tr>
                      </thead>
                      <tbody>
                        {(series.rejectedEditors || []).map((editor) => (
                          <tr key={editor.editorId}>
                            <td>{editor.name}</td>
                            <td className="text-danger fst-italic">
                              {editor.reason}
                            </td>
                            <td>{formatDateTime(editor.rejectedAt)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <h6 className="mb-3 fw-bold text-secondary">
                    <i className="fas fa-user-check me-1"></i> Chọn Biên tập
                    viên phụ trách mới
                  </h6>
                  <div
                    className="table-wrapper mb-4"
                    style={{ maxHeight: "250px" }}
                  >
                    <table className="admin-table table-sm table-hover">
                      <thead
                        className="table-light"
                        style={{ position: "sticky", top: 0, zIndex: 1 }}
                      >
                        <tr>
                          <th className="text-center" style={{ width: "60px" }}>
                            Chọn
                          </th>
                          <th>Biên tập viên</th>
                          <th>Chuyên môn</th>
                          <th className="text-center">Task đang mở</th>
                        </tr>
                      </thead>
                      <tbody>
                        {editors.map((editor) => (
                          <tr
                            key={editor.id}
                            onClick={() =>
                              handleSelectEditor(series.id, editor.id)
                            }
                            style={{
                              cursor: isRepresentative
                                ? "pointer"
                                : "not-allowed",
                              opacity: isRepresentative ? 1 : 0.8,
                            }}
                            className={
                              selectedEditor[series.id] === editor.id
                                ? "table-primary"
                                : ""
                            }
                          >
                            <td className="text-center align-middle">
                              <input
                                type="radio"
                                name={`editor-select-${series.id}`}
                                checked={
                                  selectedEditor[series.id] === editor.id
                                }
                                onChange={() =>
                                  handleSelectEditor(series.id, editor.id)
                                }
                                disabled={!isRepresentative}
                                style={{
                                  cursor: isRepresentative
                                    ? "pointer"
                                    : "not-allowed",
                                  transform: "scale(1.2)",
                                }}
                              />
                            </td>
                            <td className="align-middle">
                              <strong>{editor.username}</strong>
                              <br />
                              <small className="text-muted">
                                {editor.email}
                              </small>
                            </td>
                            <td className="align-middle">
                              {editor.specialty ? (
                                <span
                                  className="text-secondary"
                                  style={{ fontSize: "0.9rem" }}
                                >
                                  {editor.specialty}
                                </span>
                              ) : (
                                <span className="text-muted fst-italic">
                                  Chưa cập nhật
                                </span>
                              )}
                            </td>
                            <td className="text-center align-middle">
                              <span
                                className={`badge ${editor.currentTaskCount > 10 ? "bg-danger" : "bg-info text-dark"}`}
                              >
                                {editor.currentTaskCount ?? "N/A"} task
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>

                  <div className="d-flex gap-3 justify-content-end border-top pt-4">
                    <button
                      className="btn btn-outline-danger px-4"
                      disabled={
                        !isRepresentative ||
                        assigningId === series.id ||
                        rejectingId === series.id
                      }
                      onClick={() => handleRejectFinal(series.id)}
                    >
                      <i className="fas fa-ban me-2"></i>{" "}
                      {rejectingId === series.id
                        ? "Đang xử lý..."
                        : "Đình bản Series"}
                    </button>
                    <button
                      className="btn btn-success px-4 shadow-sm"
                      disabled={
                        !isRepresentative ||
                        assigningId === series.id ||
                        rejectingId === series.id ||
                        !selectedEditor[series.id]
                      }
                      onClick={() => handleAssign(series.id)}
                    >
                      <i className="fas fa-check me-2"></i>{" "}
                      {assigningId === series.id
                        ? "Đang phân công..."
                        : "Xác nhận ép phân công"}
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </>
      ) : activeTab === "drop_requested" ? (
        <>
          <p className="text-muted mb-3">
            Danh sách các tác phẩm đang chạy nhưng bị BTV báo cáo xin hủy.
            Trưởng ban có thể đồng ý hủy hoặc tước quyền BTV đó để giao cho
            người khác.
          </p>
          <div className="table-wrapper">
            <table className="admin-table table-hover">
              <thead
                className="table-light"
                style={{ position: "sticky", top: 0, zIndex: 1 }}
              >
                <tr>
                  <th>ID</th>
                  <th>Bìa</th>
                  <th>Tên Series</th>
                  <th>BTV / Tác giả</th>
                  <th>Quyết định của bạn</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {dropList.length === 0 ? (
                  <tr>
                    <td
                      colSpan="6"
                      className="text-center py-5 text-muted fst-italic"
                    >
                      Hiện không có yêu cầu hủy dự án nào.
                    </td>
                  </tr>
                ) : (
                  dropList.map((series) => (
                    <tr key={series.id} className="align-middle">
                      <td>
                        <strong>#{series.id}</strong>
                      </td>
                      <td>
                        <img
                          src={
                            series.coverUrl
                              ? `http://localhost:8080/covers/${series.coverUrl}`
                              : "https://placehold.co/50x70?text=No+Cover"
                          }
                          alt={series.title}
                          className="shadow-sm"
                          style={{
                            width: "50px",
                            height: "70px",
                            objectFit: "cover",
                            borderRadius: "4px",
                          }}
                        />
                      </td>
                      <td>
                        <strong className="text-primary">{series.title}</strong>
                        <br />
                        <small className="text-muted">
                          {series.genres?.join(", ")}
                        </small>
                      </td>
                      <td>
                        <div style={{ fontSize: "0.9rem" }}>
                          <span className="text-danger">
                            💼 <strong>BTV:</strong> {series.tantouEditorName}
                          </span>
                          <br />
                          <span className="text-muted">
                            👤 <strong>Tác giả:</strong> {series.author}
                          </span>
                        </div>
                      </td>
                      <td>
                        <span className="text-muted fst-italic">
                          Chờ Trưởng ban quyết định
                        </span>
                      </td>
                      <td>
                        {isRepresentative ? (
                          <div className="d-flex flex-wrap gap-2">
                            <button
                              className="btn btn-outline-primary btn-sm fw-bold shadow-sm"
                              onClick={() => setPreviewSeries(series)}
                            >
                              <i className="fas fa-eye me-1"></i> Xem hồ sơ &
                              Báo cáo
                            </button>
                            <button
                              className="btn btn-danger btn-sm fw-bold shadow-sm"
                              onClick={() =>
                                triggerDropAction(series, "APPROVE_DROP")
                              }
                              disabled={submittingVote}
                            >
                              Đồng ý Hủy Series
                            </button>
                            <button
                              className="btn btn-success btn-sm fw-bold shadow-sm"
                              onClick={() =>
                                handleVoteDrop(series.id, "REJECT")
                              }
                              disabled={submittingVote}
                            >
                              Bác bỏ & Đổi BTV
                            </button>
                          </div>
                        ) : (
                          <span className="text-muted fst-italic">
                            Chỉ dành cho Trưởng ban
                          </span>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </>
      ) : (
        <div className="card shadow-sm border-0">
          <div className="card-body p-0">
            {historyList.length === 0 ? (
              <div className="text-center text-muted p-5 fst-italic">
                Chưa có lịch sử điều phối nhân sự nào.
              </div>
            ) : (
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="ps-4 py-3">Thời gian</th>
                    <th>ID Series</th>
                    <th>Hành động</th>
                    <th>Chi tiết / Lý do</th>
                    <th>Hồ sơ</th>
                  </tr>
                </thead>
                <tbody>
                  {historyList.map((h) => {
                    const isAssign =
                      h.action === "BOARD_FORCED_EDITOR_ASSIGNMENT";
                    const isCancel =
                      h.action === "BOARD_CANCELLED_SERIES" ||
                      h.action === "BOARD_VOTE_APPROVE";
                    return (
                      <tr key={h.id}>
                        <td
                          className="ps-4 align-middle text-muted"
                          style={{ width: "20%" }}
                        >
                          {formatDateTime(h.createdAt)}
                        </td>
                        <td className="align-middle fw-bold">#{h.seriesId}</td>
                        <td className="align-middle">
                          <span
                            className={`badge ${isAssign ? "bg-primary" : "bg-danger"}`}
                          >
                            {isAssign ? "Phân công lại BTV" : "Đình bản Series"}
                          </span>
                        </td>
                        <td className="align-middle">
                          {isAssign ? (
                            <span>
                              Giao BTV:{" "}
                              <strong className="text-primary">
                                {h.reason}
                              </strong>
                            </span>
                          ) : (
                            <span className="text-danger fst-italic">
                              {h.reason}
                            </span>
                          )}
                        </td>
                        <td className="align-middle">
                          <button
                            className="btn btn-outline-primary btn-sm shadow-sm fw-bold"
                            onClick={() => handleViewHistoryDossier(h.seriesId)}
                          >
                            <i className="fas fa-eye me-1"></i> Xem
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {previewSeries && (
        <div
          className="custom-modal-overlay"
          onClick={() => setPreviewSeries(null)}
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
              onClick={() => setPreviewSeries(null)}
            >
              ✕
            </button>
            <h4 className="mb-4 text-primary border-bottom pb-2">
              Hồ sơ tác phẩm: {previewSeries.title}
            </h4>
            <div className="row mb-4">
              <div className="col-md-4">
                <img
                  src={
                    previewSeries.coverUrl
                      ? `http://localhost:8080/covers/${previewSeries.coverUrl}`
                      : "https://placehold.co/250x350?text=No+Cover"
                  }
                  alt={previewSeries.title}
                  className="img-fluid rounded shadow"
                />
              </div>
              <div className="col-md-8">
                <div className="bg-light p-3 rounded border h-100">
                  <p className="mb-2">
                    <strong>👤 Tác giả:</strong> {previewSeries.author}
                  </p>
                  <p className="mb-2">
                    <strong>🏷️ Thể loại:</strong>{" "}
                    {previewSeries.genres?.map((g) => (
                      <span key={g} className="badge bg-secondary ms-1">
                        {g}
                      </span>
                    )) || "N/A"}
                  </p>
                  <p className="mb-0">
                    <strong>📖 Mô tả:</strong>{" "}
                    {previewSeries.description || "Chưa có mô tả."}
                  </p>
                </div>
              </div>
            </div>
            <h6 className="fw-bold mb-3 mt-4 text-uppercase text-muted">
              <i className="fas fa-folder-open me-2"></i>Hồ sơ 
            </h6>
            <div className="mb-4 bg-light p-3 rounded border">
              <SeriesFileList
                files={previewSeries.uploadedFiles || []}
                emptyText="Biên tập viên chưa đính kèm hồ sơ trình duyệt nào."
              />
            </div>
          </div>
        </div>
      )}

      {actionTarget && (
        <RejectReasonModal
          seriesTitle={actionTarget.title}
          submitting={submittingVote}
          onCancel={() => setActionTarget(null)}
          onConfirm={(reason) => {
            const decision =
              actionType === "APPROVE_DROP" ? "APPROVE" : "REJECT";
            handleVoteDrop(actionTarget.id, decision, reason);
          }}
          title={
            actionType === "APPROVE_DROP"
              ? "Đồng ý Hủy Series"
              : "Bác bỏ & Đổi BTV"
          }
          description={
            actionType === "APPROVE_DROP"
              ? "Nhập đánh giá về việc tại sao lại đồng ý hủy bỏ hoàn toàn series này."
              : "Nhập ghi chú cho quyết định giữ lại Series (không bắt buộc)."
          }
          confirmLabel={
            actionType === "APPROVE_DROP" ? "Xác nhận Hủy" : "Xác nhận Giữ lại"
          }
          confirmButtonClass={
            actionType === "APPROVE_DROP" ? "btn btn-danger" : "btn btn-success"
          }
          requireReason={actionType === "APPROVE_DROP"}
        />
      )}
    </div>
  );
}
