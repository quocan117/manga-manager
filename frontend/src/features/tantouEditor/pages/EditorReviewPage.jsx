import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import SeriesFileList from "../../../components/SeriesFileList";
import RejectReasonModal from "../../../components/RejectReasonModal"; 
import {
  getSeriesDossier,
  submitToBoard,
  requestRevision,
  requestDropProject,
} from "../../../services/tantouService";

export default function EditorReviewPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [dossier, setDossier] = useState(null);
  const [actionNote, setActionNote] = useState("");

  const [dossierFiles, setDossierFiles] = useState([]);
  const [showDropModal, setShowDropModal] = useState(false);
  const [dropping, setDropping] = useState(false);

  useEffect(() => {
    const fetchDossier = async () => {
      try {
        const data = await getSeriesDossier(id);
        setDossier(data);
      } catch (error) {
        console.error("Lỗi lấy hồ sơ:", error);
      }
    };
    fetchDossier();
  }, [id]);

  const handleFileChange = (e) => {
    setDossierFiles(Array.from(e.target.files));
  };

  const handleAction = async (isApprove) => {
    if (!actionNote && !isApprove) {
      alert("Vui lòng nhập lý do nếu yêu cầu tác giả sửa lại bản thảo!");
      return;
    }

    if (isApprove && dossierFiles.length === 0) {
      const confirmNoFile = window.confirm(
        "Bạn chưa đính kèm hồ sơ bảo vệ nào. Vẫn tiếp tục trình Hội đồng?",
      );
      if (!confirmNoFile) return;
    }

    try {
      if (isApprove) {
        await submitToBoard(id, actionNote, dossierFiles);
        alert("Đã tổng hợp hồ sơ và trình lên Hội đồng Biên tập thành công!");
      } else {
        await requestRevision(id, actionNote);
        alert("Đã gửi yêu cầu chỉnh sửa kèm comment về cho Mangaka!");
      }
      navigate("/tantou");
    } catch (error) {
      alert("Lỗi khi xử lý thao tác.");
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
      alert("Lỗi khi gửi yêu cầu hủy dự án.");
    } finally {
      setDropping(false);
    }
  };

  if (!dossier)
    return <div className="p-4">Đang tải hồ sơ bảo vệ Series...</div>;

  return (
    <div className="p-4 bg-light min-vh-100">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Hồ Sơ Bảo Vệ Series: {dossier.series.title}</h2>
        <button
          className="btn btn-secondary"
          onClick={() => navigate("/tantou")}
        >
          Trở về
        </button>
      </div>

      <div className="row">
        <div className="col-md-4">
          <div className="card shadow-sm border-0 mb-4">
            <div className="card-header bg-white fw-bold">
              Tổng quan số liệu
            </div>
            <div className="card-body">
              <p className="text-muted fst-italic">
                Số liệu này sẽ được dùng để bảo vệ tiềm năng của Series trước
                Hội đồng Biên tập.
              </p>
            </div>
          </div>

          <div className="card shadow-sm border-0">
            <div className="card-header bg-white fw-bold">
              Quyết định của Biên tập viên
            </div>
            <div className="card-body">
              <div className="mb-3">
                <label className="form-label fw-bold">
                  Tài liệu bảo vệ/Báo cáo
                </label>
                <input
                  type="file"
                  multiple
                  className="form-control"
                  onChange={handleFileChange}
                />
                <small className="text-muted">
                  Đính kèm hồ sơ bảo vệ (hông phải bản thảo của Mangaka) để
                  trình Hội đồng.
                </small>
              </div>

              <textarea
                className="form-control mb-3"
                rows="4"
                placeholder="Nhập nhận xét tổng quan hoặc chỉ đạo sửa chữa..."
                value={actionNote}
                onChange={(e) => setActionNote(e.target.value)}
              />

              <div className="d-flex gap-2 mb-2">
                <button
                  className="btn btn-success flex-grow-1 fw-bold"
                  onClick={() => handleAction(true)}
                >
                  ✅ Trình Hội đồng
                </button>
                <button
                  className="btn btn-danger flex-grow-1 fw-bold"
                  onClick={() => setShowDropModal(true)}
                >
                  🗑️ Yêu cầu Hủy
                </button>
              </div>

              <button
                className="btn btn-outline-danger w-100"
                onClick={() => handleAction(false)}
              >
                ❌ Yêu cầu sửa lại
              </button>
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
