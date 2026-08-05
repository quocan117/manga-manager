import React, { useState, useEffect } from "react";
import {
  getEditorAssignmentRequiredSeries,
  assignEditor,
  getUsers,
  cancelSeries,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import { useAuth } from "../../../context/AuthContext";
import "../styles/EditorialBoard.css";

export default function EditorAssignmentPage() {
  const { user } = useAuth();
  const [seriesList, setSeriesList] = useState([]);
  const [editors, setEditors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedEditor, setSelectedEditor] = useState({});
  const [assigningId, setAssigningId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null);

  const [previewSeries, setPreviewSeries] = useState(null);

  const isRepresentative = user?.email === "editorial1@manga.test";

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
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
      console.error("Lỗi phân công biên tập viên:", error);
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
      console.error("Lỗi từ chối tác phẩm:", error);
      alert(
        error?.response?.data?.message || "Không thể từ chối tác phẩm lúc này.",
      );
    } finally {
      setRejectingId(null);
    }
  };

  if (loading)
    return (
      <div className="tab-content">
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="mt-3 text-muted">Đang tải dữ liệu...</p>
        </div>
      </div>
    );

  return (
    <div className="tab-content">
      <h2 className="mb-4">🧭 Phân Công Biên Tập Viên</h2>
      <p className="text-muted mb-3">
        Danh sách các tác phẩm cần Hội đồng xem xét và chỉ định Biên tập viên
        phụ trách mới, hoặc quyết định từ chối tác phẩm nếu không phù hợp.
      </p>

      {!isRepresentative && (
        <div className="alert alert-warning shadow-sm border-0 mb-4 d-flex align-items-center">
          <i className="fas fa-exclamation-triangle fs-4 me-3 text-warning"></i>
          <div>
            <strong>Chế độ xem (View-only):</strong> Chỉ tài khoản{" "}
            <strong>Editorial Board 1</strong> (Người đại diện) mới có quyền
            thao tác phân công hoặc từ chối tác phẩm tại đây.
          </div>
        </div>
      )}

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
                <i className="fas fa-eye me-1"></i> Xem hồ sơ tác phẩm
              </button>
            </div>
            <div className="card-body">
              <h6 className="mb-3 fw-bold text-secondary">
                <i className="fas fa-history me-1"></i> Lịch sử phản hồi (
                {series.rejectedEditors?.length || 0} lần)
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
                <i className="fas fa-user-check me-1"></i> Chọn Biên tập viên
                phụ trách
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
                        onClick={() => handleSelectEditor(series.id, editor.id)}
                        style={{
                          cursor: isRepresentative ? "pointer" : "not-allowed",
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
                            checked={selectedEditor[series.id] === editor.id}
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
                          <small className="text-muted">{editor.email}</small>
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
                  <i className="fas fa-ban me-2"></i>
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
                  <i className="fas fa-check me-2"></i>
                  {assigningId === series.id
                    ? "Đang phân công..."
                    : "Xác nhận phân công"}
                </button>
              </div>
            </div>
          </div>
        ))
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
              <i className="fas fa-folder-open me-2"></i>Bản thảo & Tài liệu
              đính kèm
            </h6>
            <div className="mb-4 bg-light p-3 rounded border">
              <SeriesFileList
                files={previewSeries.uploadedFiles || []}
                emptyText="Mangaka chưa tải lên bản thảo nào."
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
