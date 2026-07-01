import { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { getPageDrawing, finalizeDrawing } from "../../services/drawingService";
import CanvasMarkupTool from "../../components/CanvasMarkupTool";
import "../styles/drawing.css";

export default function DrawingPage() {
  const { pageId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const [drawing, setDrawing] = useState(null);
  const [loading, setLoading] = useState(true);

  const originalImageUrl = location.state?.originalImageUrl
    ? `http://localhost:8080/covers/${location.state.originalImageUrl}`
    : null;

  const currentBackgroundUrl = drawing?.previewImageUrl || originalImageUrl;

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
    if (
      !window.confirm(
        "Bạn có chắc muốn chốt bản vẽ này? Sau khi chốt sẽ không thể chỉnh sửa!",
      )
    )
      return;

    try {
      await finalizeDrawing(pageId, drawing?.version || 0);
      alert("Chốt bản vẽ thành công!");
      navigate(-1);
    } catch (error) {
      alert(
        "Lỗi khi chốt bản vẽ: " +
        (error.response?.data?.message ||
          "Phiên bản đã cũ, vui lòng tải lại trang."),
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
        <h2>Khu vực vẽ / Đánh dấu - Trang {pageId}</h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          ⬅ Quay lại
        </button>
      </div>

      <div className="row">
        <div className="col-md-9">
          <div className="card shadow canvas-card">
            <div className="card-header bg-dark text-white fw-bold">
              Khu vực thao tác (Canvas)
            </div>
            <div className="card-body canvas-body">
              <CanvasMarkupTool
                pageId={pageId}
                backgroundImageUrl={currentBackgroundUrl} // Đổi thành biến mới
              />
            </div>
          </div>
        </div>

        <div className="col-md-3">
          <div className="card shadow info-card">
            <div className="card-header bg-info text-white fw-bold">
              Thông tin bản vẽ
            </div>
            <div className="card-body">
              <div className="info-item mb-2">
                <strong>Trạng thái: </strong>
                <span
                  className={`badge ${drawing?.status === "FINALIZED" ? "bg-success" : "bg-warning text-dark"} ms-1`}
                >
                  {drawing?.status ?? "BẢN NHÁP"}
                </span>
              </div>
              <div className="info-item mb-3">
                <strong>Phiên bản hiện tại: </strong> v{drawing?.version ?? 0}
              </div>

              <hr />
              <p className="text-muted small text-justify">
                * Việc lưu nháp (Save Draft) được thực hiện trực tiếp trong
                thanh công cụ của khu vực vẽ.
              </p>

              <button
                className="btn btn-danger w-100 btn-finalize mt-2"
                onClick={handleFinalize}
                disabled={drawing?.status === "FINALIZED"}
              >
                🔒 Chốt Bản Vẽ (Finalize)
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
