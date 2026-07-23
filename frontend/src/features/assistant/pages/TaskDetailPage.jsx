import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getTask,
  acceptTask,
  getTaskDrawing,
  saveTaskDrawing,
  submitTask,
  getTaskSubmissions,
} from "../../../services/assistantService";
import { resolveImageUrl } from "../../../utils/imageUrl";
import CanvasMarkupTool from "../../../components/CanvasMarkupTool";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";

const WORKABLE_STATUSES = ["ASSIGNED", "IN_PROGRESS", "REVISION_REQUESTED"];

export default function TaskDetailPage() {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const [task, setTask] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [drawingVersion, setDrawingVersion] = useState(0);
  const [note, setNote] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [resultFiles, setResultFiles] = useState([]);
  const [fileError, setFileError] = useState("");
  const backgroundUrl = resolveImageUrl(task?.pageImageUrl);

  const loadTask = useCallback(async () => {
    try {
      setLoading(true);
      const [taskData, submissionData, drawingData] = await Promise.all([
        getTask(taskId),
        getTaskSubmissions(taskId),
        getTaskDrawing(taskId),
      ]);
      setTask(taskData);
      setSubmissions(submissionData || []);
      setDrawingVersion(drawingData?.version || 0);
    } catch (error) {
      console.error("Lỗi khi tải chi tiết nhiệm vụ:", error);
    } finally {
      setLoading(false);
    }
  }, [taskId]);

  useEffect(() => {
    loadTask();
  }, [loadTask]);

  const handleAccept = async () => {
    try {
      const updated = await acceptTask(taskId);
      setTask(updated);
      alert("Đã nhận Task! Bạn có thể bắt đầu làm việc.");
    } catch (error) {
      alert("Nhận Task thất bại: " + (error.response?.data?.message || ""));
    }
  };

  const handleFilePick = (e) => {
    const picked = Array.from(e.target.files || []);
    if (picked.length === 0) return;
    setResultFiles((prev) => {
      const combined = [...prev, ...picked];
      const zipCount = combined.filter((f) =>
        f.name.toLowerCase().endsWith(".zip"),
      ).length;
      if (zipCount > 1) {
        setFileError("Chỉ được chọn tối đa 1 file .zip.");
        return prev;
      }
      if (combined.length > 20) {
        setFileError("Chỉ được chọn tối đa 20 file.");
        return prev;
      }
      setFileError("");
      return combined;
    });
    e.target.value = "";
  };

  const removeResultFile = (index) => {
    setResultFiles((prev) => prev.filter((_, i) => i !== index));
    setFileError("");
  };

  const handleSubmit = async () => {
    if (resultFiles.length === 0) {
      alert(
        "Vui lòng đính kèm ít nhất 1 ảnh đã chỉnh sửa hoặc 1 file .zip trước khi nộp.",
      );
      return;
    }
    setSubmitting(true);
    try {
      await submitTask(taskId, resultFiles, note, drawingVersion);
      alert("Nộp thành công! Mangaka sẽ nhận được file của bạn.");
      setNote("");
      setResultFiles([]);
      loadTask();
    } catch (error) {
      alert("Nộp thất bại: " + (error.response?.data?.message || ""));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading)
    return <div className="text-center mt-5">Đang tải nhiệm vụ...</div>;

  if (!task)
    return (
      <div className="text-center mt-5 text-muted">
        Không tìm thấy nhiệm vụ.
      </div>
    );

  const canWork = WORKABLE_STATUSES.includes(task.status);
  const latestSubmission = submissions[0];

  return (
    <div className="container-fluid mt-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div>
          <h2 className="mb-0">{task.title}</h2>
          <small className="text-muted">
            {task.seriesTitle} - Chapter {task.chapterNumber} - Trang{" "}
            {task.pageNumber}
          </small>
        </div>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          ⬅ Quay lại
        </button>
      </div>
      <div className="row">
        <div className="col-md-4 mb-4">
          <div className="card shadow">
            <div className="card-header bg-white fw-bold">
              Thông tin nhiệm vụ
            </div>
            <div className="card-body">
              <p>
                <strong>Loại:</strong>{" "}
                <span className="badge bg-secondary">{task.taskType}</span>
              </p>
              <p>
                <strong>Trạng thái:</strong>{" "}
                <span className="badge bg-primary">{task.status}</span>
              </p>
              <p>
                <strong>Hạn chót:</strong>{" "}
                {task.dueDate ? formatDateTime(task.dueDate) : "-"}
              </p>
              <p>
                <strong>Mô tả:</strong>
                <br />
                {task.description || "Không có mô tả."}
              </p>
              <hr />
              <p className="mb-1">
                <strong>Tài liệu gốc để làm việc:</strong>
              </p>
              <SeriesFileList files={task.sourceFiles} />

              {task.status === "ASSIGNED" && (
                <button
                  className="btn btn-success w-100 mt-3"
                  onClick={handleAccept}
                >
                  ✅ Nhận Task
                </button>
              )}
              {latestSubmission && (
                <>
                  <hr />
                  <p className="mb-1">
                    <strong>Lần nộp gần nhất:</strong>
                  </p>
                  <p className="small mb-1">
                    Trạng thái:{" "}
                    <span className="badge bg-info text-dark">
                      {latestSubmission.status}
                    </span>
                  </p>
                  {latestSubmission.reviewNote && (
                    <p className="small text-danger mb-0">
                      Ghi chú duyệt: {latestSubmission.reviewNote}
                    </p>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
        <div className="col-md-8">
          <div className="card shadow mb-3">
            <div className="card-header bg-dark text-white fw-bold">
              Khu vực xem bản vẽ
            </div>
            <div className="card-body">
              <CanvasMarkupTool
                backgroundImageUrl={backgroundUrl}
                readOnly={true}
                hideControls={true}
              />
            </div>
          </div>
          {canWork && (
            <div className="card shadow">
              <div className="card-header bg-white fw-bold">
                Nộp cho Mangaka
              </div>
              <div className="card-body">

                <div className="mb-3">
                  <label className="form-label">
                    File đã hoàn thiện (ảnh đã sửa, có thể thêm 1 file .zip)
                  </label>
                  <input
                    type="file"
                    className="form-control"
                    multiple
                    accept="image/*,.zip,.pdf,.doc,.docx,.txt,.md"
                    onChange={handleFilePick}
                  />
                  <small className="text-muted d-block mt-1">
                    Có thể chọn nhiều lần để gộp thêm ảnh/zip (tối đa 20 file,
                    mỗi ảnh ≤20MB, zip ≤100MB, tổng ≤200MB, chỉ 1 file .zip).
                  </small>
                  {fileError && (
                    <div className="text-danger small mt-1">{fileError}</div>
                  )}
                  {resultFiles.length > 0 && (
                    <ul className="submit-series-filelist mt-2">
                      {resultFiles.map((f, i) => (
                        <li key={`${f.name}-${i}`}>
                          <span>{f.name}</span>
                          <span className="text-muted small">
                            ({Math.round(f.size / 1024)} KB)
                          </span>
                          <button
                            type="button"
                            className="btn-close btn-sm"
                            aria-label="Xoá"
                            onClick={() => removeResultFile(i)}
                          />
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <div className="mb-3">
                  <label className="form-label">Ghi chú cho Mangaka</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    value={note}
                    onChange={(e) => setNote(e.target.value)}
                  />
                </div>
                <button
                  className="btn btn-primary w-100 fw-bold"
                  onClick={handleSubmit}
                  disabled={submitting}
                >
                  {submitting ? "Đang nộp..." : "📤 Nộp"}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
