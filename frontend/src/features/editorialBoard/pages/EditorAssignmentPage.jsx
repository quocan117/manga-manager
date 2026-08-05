import React, { useState, useEffect } from "react";
import {
  getEditorAssignmentRequiredSeries,
  assignEditor,
  getUsers,
  cancelSeries, 
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import "../styles/EditorialBoard.css";

export default function EditorAssignmentPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [editors, setEditors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedEditor, setSelectedEditor] = useState({});
  const [assigningId, setAssigningId] = useState(null);
  const [rejectingId, setRejectingId] = useState(null); 

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
      console.error("Lỗi tải danh sách hồ sơ cần Hội đồng phân công:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = async (seriesId) => {
    const editorId = selectedEditor[seriesId];
    if (!editorId) {
      alert("Vui lòng chọn một biên tập viên trước khi phân công.");
      return;
    }
    setAssigningId(seriesId);
    try {
      await assignEditor(seriesId, Number(editorId));
      alert("Đã phân công biên tập viên thành công!");
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
    if (
      !window.confirm(
        "Bạn có chắc chắn muốn đánh rớt (Reject Final) series này? Tác phẩm sẽ chuyển sang trạng thái Hủy.",
      )
    )
      return;

    setRejectingId(seriesId);
    try {
      await cancelSeries(
        seriesId,
        "Hội đồng đánh rớt (Reject Final) do không tìm được BTV phù hợp chuyên môn hoặc vòng lặp từ chối quá 3 lần.",
      );
      alert("Đã đánh rớt tác phẩm thành công!");
      setSeriesList((prev) => prev.filter((s) => s.id !== seriesId));
    } catch (error) {
      console.error("Lỗi đánh rớt tác phẩm:", error);
      alert(
        error?.response?.data?.message ||
          "Không thể đánh rớt tác phẩm lúc này.",
      );
    } finally {
      setRejectingId(null);
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
      <h2 className="mb-4">🧭 Phân Công Biên Tập Viên (Xử lý kẹt phân công)</h2>

      {seriesList.length === 0 ? (
        <p className="text-muted">
          Hiện không có hồ sơ nào cần Hội đồng Biên tập phân công trực tiếp.
        </p>
      ) : (
        seriesList.map((series) => (
          <div key={series.id} className="card shadow-sm border-0 mb-4">
            <div className="card-header bg-white fw-bold d-flex justify-content-between align-items-center">
              <span>
                #{series.id} — {series.title} ({series.author})
              </span>
              <span className="badge bg-warning text-dark">
                Waiting for Board Assignment
              </span>
            </div>
            <div className="card-body">
              <h6 className="mb-2">
                Danh sách biên tập viên đã từ chối (
                {series.rejectedEditors?.length || 0})
              </h6>

              <div className="table-wrapper mb-3">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Họ tên</th>
                      <th>Email</th>
                      <th>Chuyên môn</th> 
                      <th>Số task hiện tại</th>
                      <th>Lý do từ chối</th>
                      <th>Thời gian từ chối</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(series.rejectedEditors || []).map((editor) => (
                      <tr key={editor.editorId}>
                        <td>{editor.name}</td>
                        <td>{editor.email}</td>
                        <td>{editor.specialty || "Chưa cập nhật"}</td>
                        <td>{editor.currentTaskCount}</td>
                        <td>{editor.reason}</td>
                        <td>{formatDateTime(editor.rejectedAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="d-flex gap-2 align-items-center mt-3">
                <select
                  className="form-select"
                  style={{ maxWidth: "320px" }}
                  value={selectedEditor[series.id] || ""}
                  onChange={(e) =>
                    setSelectedEditor((prev) => ({
                      ...prev,
                      [series.id]: e.target.value,
                    }))
                  }
                >
                  <option value="">-- Chọn biên tập viên để ép nhận --</option>
                  {editors.map((editor) => (
                    <option key={editor.id} value={editor.id}>
                      {editor.username} ({editor.email}) -{" "}
                      {editor.specialty || "N/A"}
                    </option>
                  ))}
                </select>

                <button
                  className="btn btn-success"
                  disabled={
                    assigningId === series.id || rejectingId === series.id
                  }
                  onClick={() => handleAssign(series.id)}
                >
                  {assigningId === series.id
                    ? "Đang phân công..."
                    : "Ép nhận (Force Assign)"}
                </button>

                <button
                  className="btn btn-danger"
                  disabled={
                    assigningId === series.id || rejectingId === series.id
                  }
                  onClick={() => handleRejectFinal(series.id)}
                >
                  {rejectingId === series.id
                    ? "Đang xử lý..."
                    : "Đánh rớt (Reject Final)"}
                </button>
              </div>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
