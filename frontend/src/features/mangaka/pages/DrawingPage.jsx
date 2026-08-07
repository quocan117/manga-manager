import { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import {
  getPageDrawing,
  finalizeDrawing,
} from "../../../services/drawingService";
import { resolveImageUrl } from "../../../utils/imageUrl";
import CanvasMarkupTool from "../../../components/CanvasMarkupTool";
import api from "../../../services/api";
import "../styles/drawing.css";

export default function DrawingPage() {
  const { pageId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [drawing, setDrawing] = useState(null);
  const [loading, setLoading] = useState(true);
  const displayPageNum = location.state?.pageNumber || pageId;
  const originalImageUrl = resolveImageUrl(location.state?.originalImageUrl);
  const isFinalized = drawing?.status === "FINALIZED";

  useEffect(() => {
    loadDrawing();
  }, [pageId]);

  const loadDrawing = async () => {
    try {
      setLoading(true);
      const data = await getPageDrawing(pageId);
      setDrawing(data);
    } catch (error) {
      console.log(error);
      if (error.response?.status === 404) {
        setDrawing(null);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleFinalize = async () => {
    if (!window.confirm("XÁC NHẬN CHỐT TRANG")) return;
    try {
      const data = await finalizeDrawing(pageId, drawing?.version || 0);
      setDrawing(data);
      alert("Chốt trang hoàn thiện thành công!");
      navigate(-1);
    } catch (error) {
      alert(
        "Lỗi khi chốt bản vẽ: " +
          (error.response?.data?.message ||
            "Phiên bản đã cũ, vui lòng tải lại trang."),
      );
    }
  };

  const handleUnlock = async () => {
    if (
      !window.confirm(
        "XÁC NHẬN MỞ CHỐT TRANG?\nTrang sẽ quay về trạng thái Bản Nháp để bạn có thể đổi ảnh hoặc chỉnh sửa.",
      )
    )
      return;
    try {
      const payload = {
        canvasData: drawing?.canvasData || {},
        previewImageUrl: drawing?.previewImageUrl || "",
        expectedVersion: drawing?.version || 0,
      };
      await api.put(`/mangaka/pages/${pageId}/drawing`, payload);
      alert("Đã mở chốt trang thành công!");
      loadDrawing(); 
    } catch (error) {
      console.error("Lỗi khi mở chốt:", error);
      alert(
        "Lỗi khi mở chốt: " +
          (error.response?.data?.message || "Vui lòng tải lại trang."),
      );
    }
  };

  if (loading) {
    return (
      <div className="drawing-loading">
        <div className="spinner-border text-primary" />
        <p className="mt-3">Đang tải không gian vẽ...</p>
      </div>
    );
  }

  return (
    <div className="container-fluid mt-4 drawing-page-container">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2>Khu Đánh dấu - Trang {displayPageNum}</h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          ⬅ Quay lại
        </button>
      </div>
      <div className="row">
        <div className="col-md-9">
          <div className="card shadow canvas-card mb-4">
            <div className="card-header bg-dark text-white fw-bold d-flex justify-content-between align-items-center">
              <span>Khu vực thao tác (Đánh dấu lỗi cho Trợ lý)</span>
              {isFinalized && <span className="badge bg-success">Đã Khóa</span>}
            </div>
            <div className="card-body canvas-body">
              <CanvasMarkupTool
                pageId={pageId}
                backgroundImageUrl={originalImageUrl}
                readOnly={isFinalized}
                onStatusChange={(status) =>
                  setDrawing((prev) => (prev ? { ...prev, status } : prev))
                }
                onVersionChange={(version) =>
                  setDrawing((prev) => (prev ? { ...prev, version } : prev))
                }
              />
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow info-card border-primary">
            <div className="card-header bg-primary text-white fw-bold">
              Tiến độ Trang
            </div>
            <div className="card-body">
              <div className="info-item mb-3 d-flex flex-column gap-2">
                <span className="text-muted small fw-bold text-uppercase">
                  Trạng thái hiện tại:
                </span>
                <span
                  className={`badge fs-6 py-2 ${isFinalized ? "bg-success" : "bg-warning text-dark border border-warning"}`}
                >
                  {isFinalized
                    ? "ĐÃ HOÀN THIỆN"
                    : (drawing?.status ?? "ĐANG XỬ LÝ")}
                </span>
              </div>
              <div className="alert alert-info small py-2 mb-3">
                <i className="fas fa-info-circle me-1"></i>
                Nút <strong>"Lưu đánh dấu"</strong> bên trái dùng để lưu nét vẽ
                nháp cho trợ lý xem. Nút chốt bên dưới dùng để khóa trang nộp
                Biên tập.
              </div>
              <hr />

              {isFinalized ? (
                <button
                  className="btn btn-warning w-100 fw-bold py-2 shadow-sm text-dark"
                  onClick={handleUnlock}
                >
                  <i className="fas fa-unlock me-2"></i> Mở Chốt Trang
                </button>
              ) : (
                <button
                  className="btn btn-danger w-100 fw-bold py-2 shadow-sm"
                  onClick={handleFinalize}
                >
                  <i className="fas fa-check-circle me-2"></i> Chốt Bản Hoàn
                  Thiện
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
