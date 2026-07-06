import { useState } from "react";
import CanvasMarkupTool from "./CanvasMarkupTool";

export default function ChapterMarkupUploader({ onSaveNote, onSendAll }) {
  const [images, setImages] = useState([]); 
  const [currentIndex, setCurrentIndex] = useState(0);
  const [sending, setSending] = useState(false);

  const handleAddFiles = (e) => {
    const files = Array.from(e.target.files || []);
    if (files.length === 0) return;

    const newItems = files.map((file) => ({
      file,
      previewUrl: URL.createObjectURL(file),
      note: null,
      finalized: false,
    }));

    setImages((prev) => {
      const wasEmpty = prev.length === 0;
      const updated = [...prev, ...newItems];
      if (wasEmpty) setCurrentIndex(0);
      return updated;
    });
    e.target.value = "";
  };

  const currentImage = images[currentIndex];
  const allFinalized =
    images.length > 0 && images.every((img) => img.finalized);

  const handlePersist = async (canvasJSON, previewImageUrl) => {
    const note = await onSaveNote(currentIndex, {
      imageFile: currentImage.file,
      canvasData: canvasJSON,
      previewImageUrl,
    });

    setImages((prev) =>
      prev.map((img, idx) =>
        idx === currentIndex ? { ...img, note, finalized: true } : img,
      ),
    );

    return { version: 1, status: "FINALIZED" };
  };

  const handleNext = () => {
    if (currentIndex < images.length - 1) setCurrentIndex((i) => i + 1);
  };

  const handleSendAll = async () => {
    setSending(true);
    try {
      await onSendAll(images.map((img) => img.note));
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="chapter-markup-uploader">
      <div className="mb-3 p-3 border rounded bg-light">
        <label className="fw-bold mb-2 d-block">
          Upload ảnh trang truyện cần chỉnh sửa (chọn từ máy)
        </label>
        <input
          type="file"
          multiple
          accept="image/png, image/jpeg, image/webp"
          onChange={handleAddFiles}
          className="form-control"
        />
        {images.length > 0 && (
          <small className="text-muted d-block mt-2">
            Đã thêm {images.length} ảnh — đã chốt{" "}
            {images.filter((i) => i.finalized).length}/{images.length}
          </small>
        )}
      </div>

      {images.length === 0 && (
        <div className="alert alert-secondary">
          Chưa có ảnh nào. Hãy chọn ít nhất 1 ảnh cần chỉnh sửa để bắt đầu đánh
          dấu.
        </div>
      )}

      {currentImage && (
        <div className="card shadow-sm mb-3">
          <div className="card-header d-flex justify-content-between align-items-center">
            <span>
              Ảnh {currentIndex + 1}/{images.length} — {currentImage.file.name}
              {currentImage.finalized && (
                <span className="badge bg-success ms-2">Đã chốt</span>
              )}
            </span>
            {currentIndex < images.length - 1 && currentImage.finalized && (
              <button className="btn btn-sm btn-primary" onClick={handleNext}>
                Ảnh tiếp theo ▶
              </button>
            )}
          </div>
          <div className="card-body">
            <CanvasMarkupTool
              key={currentIndex}
              pageId={`local-${currentIndex}`}
              backgroundImageUrl={currentImage.previewUrl}
              readOnly={currentImage.finalized}
              hideControls={currentImage.finalized}
              loadDrawing={async () => null}
              persistDrawing={handlePersist}
            />
          </div>
        </div>
      )}

      {images.length > 0 && (
        <button
          className="btn btn-success w-100 fw-bold"
          disabled={!allFinalized || sending}
          onClick={handleSendAll}
        >
          {sending
            ? "Đang gửi..."
            : allFinalized
              ? "📤 Gửi toàn bộ ảnh đánh dấu cho Mangaka"
              : "⚠️ Hãy chốt hết tất cả ảnh trước khi gửi"}
        </button>
      )}
    </div>
  );
}
