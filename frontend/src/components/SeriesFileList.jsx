import { useEffect, useState } from "react";
import { fetchSeriesFileBlob } from "../services/seriesFileService";
import { isPreviewableFile } from "../utils/fileUrl";
import "../styles/SeriesFileList.css";

function useObjectUrl(fileId, enabled) {
  const [url, setUrl] = useState(null);
  const [error, setError] = useState(null);
  useEffect(() => {
    if (!enabled) return;
    let objectUrl;
    let cancelled = false;
    fetchSeriesFileBlob(fileId)
      .then((blob) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(blob);
        setUrl(objectUrl);
      })
      .catch((err) => !cancelled && setError(err.message));
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [fileId, enabled]);
  return { url, error };
}

function PreviewThumb({ file, onClick }) {
  const { url } = useObjectUrl(file.id, true);
  if (file.contentType === "application/pdf") {
    return (
      <div className="series-file-thumb" onClick={() => onClick(file)}>
        <div className="series-file-pdf-icon">📄 PDF</div>
        <p className="small text-truncate mb-0">{file.originalFileName}</p>
      </div>
    );
  }
  return (
    <div className="series-file-thumb" onClick={() => onClick(file)}>
      {url ? (
        <img src={url} alt={file.originalFileName} />
      ) : (
        <div className="series-file-loading">Đang tải...</div>
      )}
      <p className="small text-truncate mb-0">{file.originalFileName}</p>
    </div>
  );
}

function DownloadButton({ file }) {
  const [downloading, setDownloading] = useState(false);
  const handleDownload = async () => {
    try {
      setDownloading(true);
      const blob = await fetchSeriesFileBlob(file.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = file.originalFileName || "file";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert(err.message);
    } finally {
      setDownloading(false);
    }
  };
  return (
    <button
      className="btn btn-outline-secondary btn-sm d-flex align-items-center gap-2 mb-2"
      onClick={handleDownload}
      disabled={downloading}
    >
      📦 {file.originalFileName}{" "}
      <span className="text-muted">
        ({Math.round(file.fileSize / 1024)} KB)
      </span>
      <span className="ms-auto">
        {downloading ? "Đang tải..." : "Tải xuống"}
      </span>
    </button>
  );
}

export default function SeriesFileList({ files = [] }) {
  const [previewFile, setPreviewFile] = useState(null);
  const { url: modalUrl } = useObjectUrl(previewFile?.id, !!previewFile);
  if (!files.length) {
    return <p className="text-muted mb-0">Mangaka chưa gửi kèm file nào.</p>;
  }
  const previewables = files.filter(isPreviewableFile);
  const others = files.filter((f) => !isPreviewableFile(f));
  return (
    <div>
      {previewables.length > 0 && (
        <div className="series-file-grid mb-3">
          {previewables.map((f) => (
            <PreviewThumb key={f.id} file={f} onClick={setPreviewFile} />
          ))}
        </div>
      )}
      {others.length > 0 && (
        <div className="series-file-others">
          {others.map((f) => (
            <DownloadButton key={f.id} file={f} />
          ))}
        </div>
      )}
      {previewFile && (
        <div
          className="series-file-preview-overlay"
          onClick={() => setPreviewFile(null)}
        >
          <div
            className="series-file-preview-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <button className="close-btn" onClick={() => setPreviewFile(null)}>
              ✕
            </button>
            {previewFile.contentType === "application/pdf"
              ? modalUrl && (
                  <iframe title={previewFile.originalFileName} src={modalUrl} />
                )
              : modalUrl && (
                  <img src={modalUrl} alt={previewFile.originalFileName} />
                )}
          </div>
        </div>
      )}
    </div>
  );
}
