import React, { useEffect, useRef, useState } from "react";
import * as fabric from "fabric";
import { savePageDrawing, getPageDrawing } from "../services/drawingService";
import "../styles/CanvasMarkup.css";

const CanvasMarkupTool = ({ pageId, backgroundImageUrl }) => {
  const canvasRef = useRef(null);
  const [canvas, setCanvas] = useState(null);
  const [isDrawingMode, setIsDrawingMode] = useState(false);
  const [currentVersion, setCurrentVersion] = useState(0);

  useEffect(() => {
    const initCanvas = new fabric.Canvas(canvasRef.current, {
      width: 800,
      height: 1200,
      isDrawingMode: false,
    });

    initCanvas.freeDrawingBrush.color = "red";
    initCanvas.freeDrawingBrush.width = 3;

    setCanvas(initCanvas);

    const loadExistingData = async () => {
      try {
        const data = await getPageDrawing(pageId);
        if (data && data.canvasData) {
          initCanvas.loadFromJSON(data.canvasData, () => {
            initCanvas.renderAll();
          });
          setCurrentVersion(data.version);
        }
      } catch (error) {
        console.log("Chưa có dữ liệu markup cũ hoặc có lỗi xảy ra.");
      }
    };

    if (backgroundImageUrl) {
      fabric.Image.fromURL(backgroundImageUrl, (img) => {
        img.scaleToWidth(800);
        initCanvas.setBackgroundImage(
          img,
          initCanvas.renderAll.bind(initCanvas),
        );
      });
    }

    loadExistingData();

    return () => {
      initCanvas.dispose();
    };
  }, [pageId, backgroundImageUrl]);

  const toggleDrawingMode = () => {
    if (canvas) {
      canvas.isDrawingMode = !isDrawingMode;
      setIsDrawingMode(!isDrawingMode);
    }
  };

  const handleSave = async () => {
    if (!canvas) return;

    const canvasJSON = canvas.toJSON();

    const previewImageUrl = canvas.toDataURL({
      format: "jpeg",
      quality: 0.5,
    });

    try {
      const response = await savePageDrawing(
        pageId,
        canvasJSON,
        previewImageUrl,
        currentVersion,
      );
      setCurrentVersion(response.version);
      alert("Lưu markup thành công!");
    } catch (error) {
      alert("Lưu thất bại! " + (error.response?.data?.message || ""));
    }
  };

  return (
    <div className="canvas-markup-container">
      <div className="toolbar">
        <button
          onClick={toggleDrawingMode}
          className={`btn-mode ${isDrawingMode ? "btn-mode-drawing" : "btn-mode-normal"}`}
        >
          {isDrawingMode ? "Dừng vẽ" : "Bật chế độ vẽ"}
        </button>
        <button onClick={handleSave} className="btn-save">
          Lưu đánh dấu
        </button>
      </div>

      <div className="canvas-wrapper">
        <canvas ref={canvasRef} />
      </div>
    </div>
  );
};

export default CanvasMarkupTool;
