import React, { useEffect, useRef, useState, useCallback } from "react";
import * as fabric from "fabric";
import { savePageDrawing, getPageDrawing } from "../services/drawingService";
import "../styles/CanvasMarkup.css";
const CanvasMarkupTool = ({
  pageId,
  backgroundImageUrl,
  loadDrawing,
  persistDrawing,
  onVersionChange,
  onStatusChange,
  readOnly = false,
  hideControls = false,
}) => {
  const fetchDrawing = loadDrawing || (() => getPageDrawing(pageId));
  const persist =
    persistDrawing ||
    ((canvasJSON, previewImageUrl, expectedVersion) =>
      savePageDrawing(pageId, canvasJSON, previewImageUrl, expectedVersion));
  const canvasRef = useRef(null);
  const [canvas, setCanvas] = useState(null);
  const [isDrawingMode, setIsDrawingMode] = useState(false);
  const [currentVersion, setCurrentVersion] = useState(0);
  const [drawingStatus, setDrawingStatus] = useState(null);
  // ---- Undo/Redo state ----
  const historyRef = useRef([]);
  const historyIndexRef = useRef(-1);
  const isRestoringRef = useRef(false);
  const [canUndo, setCanUndo] = useState(false);
  const [canRedo, setCanRedo] = useState(false);
  const MAX_HISTORY = 30;
  const isLocked = readOnly || drawingStatus === "FINALIZED";
  const shouldHideControls = isLocked && hideControls;
  const isLockedRef = useRef(isLocked);
  useEffect(() => {
    isLockedRef.current = isLocked;
  }, [isLocked]);
  const pushHistory = useCallback((fabricCanvas) => {
    if (isRestoringRef.current) return;
    const json = JSON.stringify(fabricCanvas.toJSON());
    historyRef.current = historyRef.current.slice(
      0,
      historyIndexRef.current + 1,
    );
    historyRef.current.push(json);
    if (historyRef.current.length > MAX_HISTORY) {
      historyRef.current.shift();
    } else {
      historyIndexRef.current += 1;
    }
    setCanUndo(historyIndexRef.current > 0);
    setCanRedo(false);
  }, []);
  const restoreFromHistory = useCallback(async (fabricCanvas, index) => {
    if (index < 0 || index >= historyRef.current.length) return;
    isRestoringRef.current = true;
    const json = historyRef.current[index];
    await fabricCanvas.loadFromJSON(json);
    fabricCanvas.renderAll();
    isRestoringRef.current = false;
    historyIndexRef.current = index;
    setCanUndo(index > 0);
    setCanRedo(index < historyRef.current.length - 1);
  }, []);
  const handleUndo = () =>
    canvas &&
    !isLocked &&
    restoreFromHistory(canvas, historyIndexRef.current - 1);
  const handleRedo = () =>
    canvas &&
    !isLocked &&
    restoreFromHistory(canvas, historyIndexRef.current + 1);
  const handleDeleteSelected = () => {
    if (!canvas || isLocked) return;
    const activeObjects = canvas.getActiveObjects();
    if (activeObjects.length === 0) {
      alert(
        "Hãy chọn nét vẽ cần xóa trước (tắt chế độ vẽ rồi click vào nét vẽ).",
      );
      return;
    }
    isRestoringRef.current = true;
    activeObjects.forEach((obj) => canvas.remove(obj));
    canvas.discardActiveObject();
    canvas.renderAll();
    isRestoringRef.current = false;
    pushHistory(canvas);
  };
  const handleClearAll = () => {
    if (!canvas || isLocked) return;
    if (!window.confirm("Xóa toàn bộ nét đánh dấu mới trên trang này?")) return;
    isRestoringRef.current = true;
    const objects = canvas.getObjects();
    objects.forEach((obj) => {
      if (!obj.locked) {
        canvas.remove(obj);
      }
    });
    canvas.renderAll();
    isRestoringRef.current = false;
    pushHistory(canvas);
  };
  useEffect(() => {
    const initCanvas = new fabric.Canvas(canvasRef.current, {
      width: 800,
      height: 1200,
      isDrawingMode: false,
    });
    const brush = new fabric.PencilBrush(initCanvas);
    brush.color = "red";
    brush.width = 3;
    initCanvas.freeDrawingBrush = brush;
    setCanvas(initCanvas);
    const loadBackground = async () => {
      if (!backgroundImageUrl) return;
      const img = await fabric.Image.fromURL(backgroundImageUrl, {
        crossOrigin: "anonymous",
      });
      const canvasWidth = initCanvas.width;
      const canvasHeight = initCanvas.height;
      img.set({
        scaleX: canvasWidth / img.width,
        scaleY: canvasHeight / img.height,
        originX: "left",
        originY: "top",
      });
      initCanvas.backgroundImage = img;
      initCanvas.renderAll();
    };
    const setupCanvasData = async () => {
      isRestoringRef.current = true;
      try {
        const data = await fetchDrawing();
        if (data && data.canvasData) {
          await initCanvas.loadFromJSON(data.canvasData);
          initCanvas.getObjects().forEach((obj) => {
            obj.set({
              selectable: false,
              evented: false,
              locked: true,
            });
          });
          setCurrentVersion(data.version);
          onVersionChange?.(data.version);
          setDrawingStatus(data.status ?? null);
          onStatusChange?.(data.status ?? null);
        }
      } catch (error) {
        console.log("Chưa có dữ liệu markup cũ hoặc lỗi mạng.");
      }
      await loadBackground();
      initCanvas.isDrawingMode = false;
      initCanvas.selection = !readOnly;
      initCanvas.renderAll();
      isRestoringRef.current = false;
      pushHistory(initCanvas);
    };
    setupCanvasData();
    const handleChange = () => pushHistory(initCanvas);
    initCanvas.on("object:added", handleChange);
    initCanvas.on("object:removed", handleChange);
    initCanvas.on("object:modified", handleChange);
    const handleKeyDown = (e) => {
      if (isLockedRef.current) return;
      const tag = document.activeElement?.tagName;
      if (tag === "INPUT" || tag === "TEXTAREA") return;
      if (e.key === "Delete" || e.key === "Backspace") {
        const active = initCanvas.getActiveObjects();
        if (active.length > 0) {
          isRestoringRef.current = true;
          active.forEach((obj) => initCanvas.remove(obj));
          initCanvas.discardActiveObject();
          initCanvas.renderAll();
          isRestoringRef.current = false;
          pushHistory(initCanvas);
        }
      } else if (e.ctrlKey && e.key === "z") {
        restoreFromHistory(initCanvas, historyIndexRef.current - 1);
      } else if (e.ctrlKey && e.key === "y") {
        restoreFromHistory(initCanvas, historyIndexRef.current + 1);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
      initCanvas.dispose();
    };
  }, [pageId, backgroundImageUrl, pushHistory, restoreFromHistory]);
  useEffect(() => {
    if (!canvas) return;
    if (isLocked) {
      canvas.isDrawingMode = false;
      canvas.selection = false;
      canvas.discardActiveObject();
      canvas.getObjects().forEach((obj) => {
        obj.set({ selectable: false, evented: false, locked: true });
      });
      setIsDrawingMode(false);
    } else {
      canvas.selection = true;
    }
    canvas.renderAll();
  }, [isLocked, canvas]);
  const toggleDrawingMode = () => {
    if (isLocked) return;
    if (canvas) {
      canvas.isDrawingMode = !isDrawingMode;
      canvas.selection = isDrawingMode;
      setIsDrawingMode(!isDrawingMode);
    }
  };
  const handleSave = async () => {
    if (!canvas || isLocked) return;
    const canvasJSON = canvas.toJSON();
    delete canvasJSON.backgroundImage;
    const previewImageUrl = canvas.toDataURL({ format: "png" });
    try {
      const response = await persist(
        canvasJSON,
        previewImageUrl,
        currentVersion,
      );
      setCurrentVersion(response.version);
      onVersionChange?.(response.version);
      setDrawingStatus(response.status ?? null);
      onStatusChange?.(response.status ?? null);
      alert("Lưu markup thành công!");
    } catch (error) {
      alert("Lưu thất bại! " + (error.response?.data?.message || ""));
    }
  };
  return (
    <div className="canvas-markup-container">
      {!shouldHideControls && (
        <div className="toolbar">
          <button
            onClick={toggleDrawingMode}
            disabled={isLocked}
            className={`btn-mode ${isDrawingMode ? "btn-mode-drawing" : "btn-mode-normal"}`}
          >
            {isDrawingMode ? "Dừng vẽ" : "Bật chế độ vẽ"}
          </button>
          <button
            onClick={handleDeleteSelected}
            disabled={isLocked}
            className="btn-delete"
          >
            Xóa nét đã chọn
          </button>
          <button
            onClick={handleClearAll}
            disabled={isLocked}
            className="btn-clear"
          >
            Xóa tất cả
          </button>
          <button
            onClick={handleUndo}
            disabled={!canUndo || isLocked}
            className="btn-undo"
          >
            ↩ Undo
          </button>
          <button
            onClick={handleRedo}
            disabled={!canRedo || isLocked}
            className="btn-redo"
          >
            ↪ Redo
          </button>
          <button onClick={handleSave} disabled={isLocked} className="btn-save">
            Lưu đánh dấu
          </button>
        </div>
      )}
      <div className="canvas-wrapper">
        <canvas ref={canvasRef} />
      </div>
    </div>
  );
};
export default CanvasMarkupTool;