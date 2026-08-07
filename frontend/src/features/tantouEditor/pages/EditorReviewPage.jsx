import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SeriesFileList from "../../../components/SeriesFileList";
import RejectReasonModal from "../../../components/RejectReasonModal";
import {
  getSeriesDossier,
  submitToBoard,
  requestRevision,
  requestDropProject,
  acceptSeries,
} from "../../../services/tantouService";

export default function EditorReviewPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [dossier, setDossier] = useState(null);
  const [actionNote, setActionNote] = useState("");
  const [dossierFiles, setDossierFiles] = useState([]);
  const [showDropModal, setShowDropModal] = useState(false);
  const [dropping, setDropping] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchDossier();
  }, [id]);

  const fetchDossier = async () => {
    try {
      const data = await getSeriesDossier(id);
      setDossier(data);
    } catch (error) {
      console.error("Lỗi lấy hồ sơ:", error);
    }
  };

  const handleFileChange = (e) => {
    const picked = Array.from(e.target.files || []);
    setDossierFiles((prev) => [...prev, ...picked]);
    e.target.value = "";
  };

  const handleRemoveFile = (index) => {
    setDossierFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleAccept = async () => {
    try {
      setSubmitting(true);
      await acceptSeries(id);
      alert("Đã chấp nhận phụ trách dự án này!");
      fetchDossier();
    } catch (error) {
      alert(error?.response?.data?.message || "Lỗi khi nhận dự án.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleAction = async (isApprove) => {
    const noteContent = actionNote.trim();
    if (isApprove) {
      if (!noteContent) {
        alert(
          "Vui lòng nhập nhận xét tổng quan hoặc báo cáo trước khi trình Hội đồng!",
        );
        return;
      }
      if (dossierFiles.length === 0) {
        alert(
          "Bắt buộc phải đính kèm tài liệu bảo vệ/báo cáo để trình lên Hội đồng!",
        );
        return;
      }
    } else {
      if (!noteContent) {
        alert(
          "Vui lòng nhập lý do chi tiết nếu yêu cầu Mangaka sửa lại bản thảo!",
        );
        return;
      }
    }

    try {
      setSubmitting(true);
      if (isApprove) {
        await submitToBoard(id, noteContent, dossierFiles);
        alert("Đã tổng hợp hồ sơ và trình lên Hội đồng Biên tập thành công!");
      } else {
        await requestRevision(id, noteContent);
        alert("Đã gửi yêu cầu chỉnh sửa kèm comment về cho Mangaka!");
      }
      navigate("/tantou");
    } catch (error) {
      alert(
        error?.response?.data?.message || "Lỗi máy chủ khi xử lý thao tác.",
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleRequestDrop = async (reason) => {
    try {
      setDropping(true);
      await requestDropProject(id, reason);
      alert("Đã gửi báo cáo Yêu cầu Hủy dự án lên Hội đồng!");
      setShowDropModal(false);
      navigate("/tantou");
    } catch (error) {
      alert(error?.response?.data?.message || "Lỗi khi gửi yêu cầu hủy dự án.");
    } finally {
      setDropping(false);
    }
  };

  if (!dossier)
    return <div className="p-4">Đang tải hồ sơ bảo vệ Series...</div>;

  const isPendingEditor =
    dossier.series.status === "PENDING_EDITOR" ||
    dossier.series.status === "EDITOR_ASSIGNMENT_REQUIRED";

  return (
    <div className="p-4 bg-light min-vh-100">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Hồ Sơ Bảo Vệ Series: {dossier.series.title}</h2>
        <button
          className="btn btn-secondary"
          onClick={() => navigate("/tantou")}
          disabled={submitting || dropping}
        >
          Trở về
        </button>
      </div>
      <div className="row">
        <div className="col-md-4">
          <div className="card shadow-sm border-0">
            <div className="card-header bg-white fw-bold">
              Quyết định của Biên tập viên
            </div>
            <div className="card-body">
              {isPendingEditor ? (
                <div className="text-center py-4">
                  <i className="fas fa-exclamation-circle text-warning fs-1 mb-3"></i>
                  <p className="mb-4">
                    Bạn cần chính thức nhận dự án này để có quyền Đánh giá và
                    Trình Hội Đồng.
                  </p>
                  <button
                    className="btn btn-primary w-100 fw-bold"
                    onClick={handleAccept}
                    disabled={submitting}
                  >
                    {submitting ? "Đang xử lý..." : "✅ Chấp nhận phụ trách"}
                  </button>
                </div>
              ) : (
                <>
                  <div className="mb-3">
                    <label className="form-label fw-bold">
                      Tài liệu bảo vệ / Báo cáo{" "}
                      <span className="text-danger">*</span>
                    </label>
                    <input
                      type="file"
                      multiple
                      className="form-control"
                      onChange={handleFileChange}
                      disabled={submitting}
                    />
                    <small className="text-muted d-block mt-1">
                      Đính kèm hồ sơ bảo vệ để trình Hội đồng.
                    </small>
                    {dossierFiles.length > 0 && (
                      <ul className="list-group mt-2">
                        {dossierFiles.map((file, idx) => (
                          <li
                            key={`${file.name}-${idx}`}
                            className="list-group-item d-flex justify-content-between align-items-center py-2 px-3"
                          >
                            <span
                              className="text-truncate"
                              style={{ maxWidth: "85%", fontSize: "0.85rem" }}
                            >
                              📄 {file.name}{" "}
                              <span className="text-muted">
                                ({Math.round(file.size / 1024)} KB)
                              </span>
                            </span>
                            <button
                              type="button"
                              className="btn-close btn-sm"
                              aria-label="Xoá"
                              onClick={() => handleRemoveFile(idx)}
                              disabled={submitting}
                              title="Xóa tệp này"
                            />
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                  <label className="form-label fw-bold">
                    Nhận xét tổng quan <span className="text-danger">*</span>
                  </label>
                  <textarea
                    className="form-control mb-3"
                    rows="4"
                    placeholder="Nhập báo cáo trình Hội đồng hoặc yêu cầu Mangaka chỉnh sửa"
                    value={actionNote}
                    onChange={(e) => setActionNote(e.target.value)}
                    disabled={submitting}
                  />
                  <div className="d-flex gap-2 mb-2">
                    <button
                      className="btn btn-success flex-grow-1 fw-bold"
                      onClick={() => handleAction(true)}
                      disabled={submitting}
                    >
                      {submitting ? "Đang xử lý..." : "Trình Hội đồng"}
                    </button>
                    <button
                      className="btn btn-danger flex-grow-1 fw-bold"
                      onClick={() => setShowDropModal(true)}
                      disabled={submitting}
                    >
                      Yêu cầu Hủy
                    </button>
                  </div>
                  <button
                    className="btn btn-outline-danger w-100 fw-bold"
                    onClick={() => handleAction(false)}
                    disabled={submitting}
                  >
                    {submitting ? "Đang gửi..." : "Yêu cầu sửa lại"}
                  </button>
                </>
              )}
            </div>
          </div>
        </div>
        <div className="col-md-8">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-header bg-white fw-bold">
              Bản thảo do Mangaka gửi
            </div>
            <div className="card-body">
              <SeriesFileList files={dossier.series.uploadedFiles} />
            </div>
          </div>
        </div>
      </div>
      {showDropModal && (
        <RejectReasonModal
          seriesTitle={dossier.series.title}
          submitting={dropping}
          onCancel={() => setShowDropModal(false)}
          onConfirm={handleRequestDrop}
          title="Yêu cầu Hủy dự án"
          description="Nhập báo cáo đánh giá hoặc lý do để yêu cầu Hội đồng hủy bỏ dự án này."
          confirmLabel="Gửi yêu cầu"
          submittingLabel="Đang gửi..."
        />
      )}
    </div>
  );
}
