import React, { useState, useEffect } from "react";
import {
  getEditorAssignmentRequiredSeries,
  assignEditor,
  getUsers,
  cancelSeries,
  getAssignmentHistory,
  getSeriesReview
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import { useAuth } from "../../../context/AuthContext";
import "../styles/EditorialBoard.css";

export default function EditorAssignmentPage() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState("pending");

  const [seriesList, setSeriesList] = useState([]);
  const [historyList, setHistoryList] = useState([]);
  const [editors, setEditors] = useState([]);
  const [loading, setLoading] = useState(true);

  const [selectedEditor, setSelectedEditor] = useState({});
  const [assigningId, setAssigningId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);
  const [previewSeries, setPreviewSeries] = useState(null);

  const isRepresentative = user?.email === "editorial1@manga.test";

  useEffect(() => {
    if (activeTab === "pending") {
      fetchPendingData();
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
      setSeriesList(seriesData || []);
      setEditors(
        (usersData || []).filter(
          (u) => u.role === "TANTOU_EDITOR" && u.status === "ACTIVE",
        ),
      );
    } catch (error) {
      console.error("Lỗi tải danh sách hồ sơ cần phân công:", error);
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
    setSelectedEditor((prev) => ({
      ...prev,
      [seriesId]: editorId,
    }));
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
      setSeriesList((prev) => prev.filter((s) => s.id !== seriesId));
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
      "Nhập lý do từ chối tác phẩm này (Sẽ gửi thông báo cho Mangaka):",
      "Hội đồng từ chối do không tìm được Biên tập viên phù hợp chuyên môn.",
    );
    if (reason === null) return;
    setRejectingId(seriesId);
    try {
      await cancelSeries(seriesId, reason);
      alert("Đã từ chối tác phẩm thành công!");
      setSeriesList((prev) => prev.filter((s) => s.id !== seriesId));
    } catch (error) {
      alert(
        error?.response?.data?.message || "Không thể từ chối tác phẩm lúc này.",
      );
    } finally {
      setRejectingId(null);
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
      <h2 className="mb-4">🧭 Quản Lý Phân Công Biên Tập</h2>

      {!isRepresentative && (
        <div className="alert alert-warning shadow-sm border-0 mb-4 d-flex align-items-center">
          <i className="fas fa-exclamation-triangle fs-4 me-3 text-warning"></i>
          <div>
            <strong>Chế độ xem (View-only):</strong> Chỉ tài khoản{" "}
            <strong>Editorial Board 1</strong> (Người đại diện) mới có quyền
            thao tác phân công hoặc xem lịch sử tại đây.
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
            <i className="fas fa-hourglass-half me-2"></i> Cần xử lý{" "}
            {activeTab === "pending" && `(${seriesList.length})`}
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
              <i className="fas fa-history me-2"></i> Lịch sử quyết định
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
            Danh sách các tác phẩm bị BTV từ chối. Trưởng ban cần chỉ định BTV
            khác hoặc từ chối tác phẩm.
          </p>
          {seriesList.length === 0 ? (
            <div className="alert alert-success border-0 shadow-sm py-4 text-center mt-4">
              <i className="fas fa-check-circle fs-3 text-success mb-2 d-block"></i>
              Hiện không có hồ sơ nào cần phân công lại.
            </div>
          ) : (
            seriesList.map((series) => (
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
                    <i className="fas fa-history me-1"></i> Lịch sử BTV phản hồi
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
                        : "Từ chối tác phẩm"}
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
                        : "Xác nhận phân công"}
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </>
      ) : (
        <div className="card shadow-sm border-0">
          <div className="card-body p-0">
            {historyList.length === 0 ? (
              <div className="text-center text-muted p-5 fst-italic">
                Chưa có lịch sử phân công hoặc từ chối nào.
              </div>
            ) : (
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="ps-4 py-3">Thời gian</th>
                    <th>ID Series</th>
                    <th>Hành động</th>
                    <th>Chi tiết</th>
                    <th>Hồ sơ</th>
                  </tr>
                </thead>
                <tbody>
                  {historyList.map((h) => {
                    const isAssign =
                      h.action === "BOARD_FORCED_EDITOR_ASSIGNMENT";
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
                            className={`badge ${isAssign ? "bg-success" : "bg-danger"}`}
                          >
                            {isAssign ? "Phân công" : "Đánh rớt Series"}
                          </span>
                        </td>
                        <td className="align-middle">
                          {isAssign ? (
                            <span>
                              Đã giao cho BTV:{" "}
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
                            <i className="fas fa-eye me-1"></i> Xem hồ sơ
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
                  src={`http://localhost:8080/covers/${previewSeries.coverUrl}`}
                  alt={previewSeries.title}
                  className="img-fluid rounded shadow"
                  onError={(e) =>
                    (e.target.src =
                      "https://placehold.co/250x350?text=No+Cover")
                  }
                />
              </div>
              <div className="col-md-8">
                <div className="bg-light p-3 rounded border h-100">
                  <p className="mb-2">
                    <strong>👤 Tác giả:</strong> {previewSeries.author}
                  </p>
                  <p className="mb-2">
                    <strong>🏷️ Thể loại:</strong>
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
              <i className="fas fa-folder-open me-2"></i>Bản thảo & Báo cáo đánh
              giá đính kèm
            </h6>
            <div className="mb-4 bg-light p-3 rounded border">
              <SeriesFileList
                files={previewSeries.uploadedFiles || []}
                emptyText="Chưa có bản thảo hoặc tài liệu báo cáo nào."
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
