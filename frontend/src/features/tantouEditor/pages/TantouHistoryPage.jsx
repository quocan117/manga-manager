import React, { useState, useEffect } from "react";
import {
  getStudioProgress,
  getPendingReviewSeries,
} from "../../../services/tantouService";
import api from "../../../services/api";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import { resolveImageUrl } from "../../../utils/imageUrl";
import "../styles/TantouEditor.css";

const getRevisionImageUrl = (url) => {
  return resolveImageUrl(url, "https://placehold.co/200x300?text=No+Image");
};

export default function TantouHistoryPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [selectedSeriesId, setSelectedSeriesId] = useState("");
  const [chapters, setChapters] = useState([]);
  const [selectedChapterId, setSelectedChapterId] = useState("");

  const [workflowHistory, setWorkflowHistory] = useState({
    submitToBoard: [],
    requestRevision: [],
  });
  const [chapterEvents, setChapterEvents] = useState([]); 

  const [historySubTab, setHistorySubTab] = useState("submit");
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [chapterHistoryLoading, setChapterHistoryLoading] = useState(false);

  useEffect(() => {
    const fetchSeriesOptions = async () => {
      try {
        const [progressData, pendingData] = await Promise.all([
          getStudioProgress().catch(() => []),
          getPendingReviewSeries().catch(() => []),
        ]);
        const map = new Map();
        progressData.forEach((s) => {
          if (s.seriesId)
            map.set(s.seriesId, { id: s.seriesId, title: s.seriesTitle });
        });
        pendingData.forEach((s) => {
          if (s.id) map.set(s.id, { id: s.id, title: s.title });
        });
        setSeriesList(Array.from(map.values()));
      } catch (err) {
        console.error("Lỗi lấy danh sách series:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchSeriesOptions();
  }, []);

  useEffect(() => {
    if (!selectedSeriesId) {
      setWorkflowHistory({ submitToBoard: [], requestRevision: [] });
      setChapters([]);
      setSelectedChapterId("");
      return;
    }

    const fetchHistoryAndChapters = async () => {
      setHistoryLoading(true);
      try {
        const resHistory = await api.get(
          `/api/series/${selectedSeriesId}/editor-workflow-history`,
        );
        setWorkflowHistory(
          resHistory.data || { submitToBoard: [], requestRevision: [] },
        );

        const resManuscript = await api.get(
          `/tantou-editor/series/${selectedSeriesId}/manuscript`,
        );
        if (resManuscript.data && resManuscript.data.chapters) {
          const sortedChapters = resManuscript.data.chapters.sort(
            (a, b) => a.chapterNumber - b.chapterNumber,
          );
          setChapters(sortedChapters);
        } else {
          setChapters([]);
        }

        setSelectedChapterId("");
      } catch (err) {
        console.error("Lỗi tải lịch sử hoặc chapter:", err);
      } finally {
        setHistoryLoading(false);
      }
    };

    fetchHistoryAndChapters();
  }, [selectedSeriesId]);

  useEffect(() => {
    if (!selectedChapterId) {
      setChapterEvents([]);
      return;
    }

    const fetchChapterHistory = async () => {
      setChapterHistoryLoading(true);
      try {
        const notesRes = await api.get(
          `/tantou-editor/chapters/${selectedChapterId}/revision-notes`,
        );
        const notes = notesRes.data || [];

        let events = [];
        notes.forEach((n) => {
          events.push({
            id: `rev_${n.id}`,
            type: "REVISION_NOTE",
            date: n.createdAt,
            data: n,
          });
        });

        const chapter = chapters.find(
          (c) => c.id.toString() === selectedChapterId,
        );
        if (chapter) {
          (chapter.pages || []).forEach((page) => {
            (page.comments || []).forEach((c) => {
              events.push({
                id: `c_${c.id}`,
                type: "COMMENT",
                date: c.createdAt,
                pageNumber: page.pageNumber,
                data: c,
              });
            });
            (page.history || []).forEach((h) => {
              events.push({
                id: `h_${h.id}`,
                type: "PAGE_UPDATE",
                date: h.createdAt,
                pageNumber: page.pageNumber,
                data: h,
              });
            });
          });

          if (
            ["APPROVED", "PUBLISHED", "PENDING_SCHEDULE"].includes(
              chapter.status?.toUpperCase(),
            )
          ) {
            let approveDateStr = new Date().toISOString();
            if (events.length > 0) {
              const maxTime = Math.max(
                ...events.map((e) => new Date(e.date).getTime()),
              );
              approveDateStr = new Date(maxTime + 60000).toISOString();
            } else if (chapter.releaseDate) {
              approveDateStr = chapter.releaseDate;
            }

            events.push({
              id: `approve_${chapter.id}`,
              type: "CHAPTER_APPROVED",
              date: approveDateStr,
              data: chapter,
            });
          }
        }
        events.sort((a, b) => new Date(b.date) - new Date(a.date));
        setChapterEvents(events);
      } catch (err) {
        console.error("Lỗi tải lịch sử chapter:", err);
      } finally {
        setChapterHistoryLoading(false);
      }
    };
    fetchChapterHistory();
  }, [selectedChapterId, chapters]);

  const renderChapterHistory = () => {
    const chapter = chapters.find((c) => c.id.toString() === selectedChapterId);
    if (!chapter) return null;

    if (chapterHistoryLoading) {
      return (
        <div className="text-center py-5">
          <div className="spinner-border text-primary" role="status"></div>
          <p className="text-muted mt-3">Đang tải lịch sử chi tiết...</p>
        </div>
      );
    }

    if (chapterEvents.length === 0) {
      return (
        <div className="text-center py-5 bg-light rounded-3 border border-light">
          <i className="fas fa-history fs-2 text-muted opacity-25 mb-3 d-block"></i>
          <p className="text-muted fst-italic mb-0">
            Chapter này chưa có lịch sử xử lý (chưa có đánh dấu lỗi hay cập nhật
            trang).
          </p>
        </div>
      );
    }

    return (
      <div className="animate-fade-in mt-2">
        <h5 className="mb-4 text-primary fw-bold border-bottom pb-3">
          <i className="fas fa-book-open me-2"></i> Lịch sử xử lý -{" "}
          {chapter.title || `Chapter ${chapter.chapterNumber}`}
        </h5>
        {chapterEvents.map((ev, idx) => (
          <div
            key={ev.id || idx}
            className="card mb-3 border border-light shadow-sm rounded-3"
          >
            <div className="card-body p-3">
              <div className="d-flex align-items-start">
                <div
                  className={`rounded-circle d-flex align-items-center justify-content-center me-3 text-white ${
                    ev.type === "REVISION_NOTE"
                      ? "bg-danger"
                      : ev.type === "COMMENT"
                        ? "bg-warning"
                        : ev.type === "CHAPTER_APPROVED"
                          ? "bg-success"
                          : "bg-info"
                  }`}
                  style={{
                    width: "45px",
                    height: "45px",
                    flexShrink: 0,
                    fontSize: "1.2rem",
                  }}
                >
                  <i
                    className={`fas ${
                      ev.type === "REVISION_NOTE"
                        ? "fa-paint-brush"
                        : ev.type === "COMMENT"
                          ? "fa-comment"
                          : ev.type === "CHAPTER_APPROVED"
                            ? "fa-check"
                            : "fa-sync-alt"
                    }`}
                  ></i>
                </div>
                <div className="flex-grow-1">
                  <h6 className="mb-1 fw-bold text-dark">
                    {ev.type === "REVISION_NOTE" &&
                      `Bạn đã đánh dấu lỗi và yêu cầu sửa`}
                    {ev.type === "COMMENT" &&
                      `Nhận xét trên Trang ${ev.pageNumber}`}
                    {ev.type === "PAGE_UPDATE" &&
                      `Cập nhật ảnh mới cho Trang ${ev.pageNumber}`}
                    {ev.type === "CHAPTER_APPROVED" &&
                      `Bạn đã Phê duyệt Chapter này`}
                  </h6>
                  <small className="text-muted d-block mb-3">
                    <i className="far fa-clock me-1"></i>{" "}
                    {formatDateTime(ev.date)}
                  </small>

                  {ev.type === "REVISION_NOTE" && (
                    <div className="bg-light rounded p-3 border-start border-danger border-3 shadow-sm">
                      <div className="text-dark fw-bold fst-italic small mb-3 lh-base">
                        "{ev.data.description}"
                      </div>
                      {ev.data.imageUrl && (
                        <a
                          href={getRevisionImageUrl(ev.data.imageUrl)}
                          target="_blank"
                          rel="noopener noreferrer"
                        >
                          <img
                            src={getRevisionImageUrl(ev.data.imageUrl)}
                            alt="Lỗi"
                            className="border rounded shadow-sm"
                            style={{
                              maxHeight: "250px",
                              objectFit: "contain",
                              cursor: "zoom-in",
                            }}
                          />
                        </a>
                      )}
                    </div>
                  )}

                  {ev.type === "COMMENT" && (
                    <div className="bg-light rounded p-2 border-start border-warning border-3 text-dark fst-italic small">
                      "{ev.data.commentText}"
                    </div>
                  )}

                  {ev.type === "PAGE_UPDATE" && ev.data.newImageUrl && (
                    <div className="mt-2">
                      <a
                        href={`http://localhost:8080/covers/pages/${ev.data.newImageUrl.split("/").pop()}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="btn btn-sm btn-outline-primary fw-bold"
                      >
                        <i className="fas fa-image me-1"></i> Xem ảnh được cập
                        nhật
                      </a>
                    </div>
                  )}

                  {ev.type === "CHAPTER_APPROVED" && (
                    <div className="bg-light rounded p-3 border-start border-success border-3 shadow-sm">
                      <div className="text-success fw-bold small">
                        <i className="fas fa-check-circle me-1"></i> Chapter đủ
                        điều kiện và đã được đưa vào hàng đợi xuất bản.
                      </div>
                    </div>
                  )}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="p-4 bg-light min-vh-100">
      <div className="dashboard-page-header mb-4">
        <div>
          <h2 className="dashboard-title">Lịch Sử Xử Lý</h2>
          <p className="dashboard-subtitle mt-1 text-muted">
            Tra cứu lại các báo cáo trình Hội đồng và các yêu cầu chỉnh sửa gửi
            cho Mangaka.
          </p>
        </div>
      </div>
      <div className="row g-4">
        <div className="col-md-4">
          <div className="card shadow-sm border-0 h-100 rounded-3">
            <div className="card-header bg-white fw-bold border-bottom-0 pt-4 pb-2 fs-5">
              1. Chọn Series
            </div>
            <div className="card-body pb-0">
              {loading ? (
                <div className="text-center text-muted py-3">
                  Đang tải danh sách...
                </div>
              ) : (
                <select
                  className="form-select border-secondary shadow-sm py-2"
                  value={selectedSeriesId}
                  onChange={(e) => setSelectedSeriesId(e.target.value)}
                >
                  <option value="">-- Vui lòng chọn Series --</option>
                  {seriesList.map((s) => (
                    <option key={s.id} value={s.id}>
                      #{s.id} - {s.title}
                    </option>
                  ))}
                </select>
              )}
              <div className="mt-3 text-muted small fst-italic">
                * Chỉ hiển thị các Series mà bạn đang trực tiếp phụ trách hoặc
                đang nằm trong danh sách cần kiểm duyệt.
              </div>
            </div>

            <div className="card-header bg-white fw-bold border-bottom-0 pt-4 pb-2 fs-5 mt-2 border-top">
              2. Chọn Chapter
            </div>
            <div className="card-body">
              <select
                className="form-select border-secondary shadow-sm py-2"
                value={selectedChapterId}
                onChange={(e) => setSelectedChapterId(e.target.value)}
                disabled={!selectedSeriesId || loading || historyLoading}
              >
                <option value="">-- Hồ sơ chung của Series --</option>
                {chapters.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.title}
                  </option>
                ))}
              </select>
              <div className="mt-3 text-muted small fst-italic">
                * Để trống nếu muốn xem lịch sử duyệt của toàn bộ Series.
              </div>
            </div>
          </div>
        </div>

        <div className="col-md-8">
          <div className="card shadow-sm border-0 min-vh-50 rounded-3">
            <div className="card-body p-4">
              {!selectedSeriesId ? (
                <div className="text-center py-5 text-muted fst-italic">
                  <i className="fas fa-mouse-pointer fs-1 mb-3 text-secondary opacity-50 d-block"></i>
                  Hãy chọn một Series ở bên trái để xem lịch sử.
                </div>
              ) : historyLoading ? (
                <div className="text-center py-5">
                  <div
                    className="spinner-border text-primary"
                    role="status"
                  ></div>
                </div>
              ) : selectedChapterId ? (
                renderChapterHistory()
              ) : (
                <div>
                  <div className="d-flex justify-content-center mb-4">
                    <ul className="nav nav-pills p-1 bg-white border rounded-pill shadow-sm d-inline-flex">
                      <li className="nav-item">
                        <button
                          className={`nav-link rounded-pill px-4 ${historySubTab === "submit" ? "active bg-primary shadow-sm" : "text-dark"}`}
                          onClick={() => setHistorySubTab("submit")}
                          style={{ fontWeight: "600", transition: "all 0.2s" }}
                        >
                          <i className="fas fa-paper-plane me-2"></i> Lịch sử
                          trình Hội đồng
                        </button>
                      </li>
                      <li className="nav-item">
                        <button
                          className={`nav-link rounded-pill px-4 ${historySubTab === "revision" ? "active bg-warning text-dark shadow-sm" : "text-dark"}`}
                          onClick={() => setHistorySubTab("revision")}
                          style={{ fontWeight: "600", transition: "all 0.2s" }}
                        >
                          <i className="fas fa-edit me-2"></i> Lịch sử yêu cầu
                          sửa
                        </button>
                      </li>
                    </ul>
                  </div>
                  {historySubTab === "submit" && (
                    <div className="animate-fade-in mt-2">
                      {workflowHistory.submitToBoard?.length > 0 ? (
                        <div>
                          {workflowHistory.submitToBoard.map((item, idx) => (
                            <div
                              key={idx}
                              className="card mb-4 border border-light shadow-sm rounded-3"
                            >
                              <div className="card-body p-4">
                                <div className="d-flex align-items-center mb-3 pb-3 border-bottom">
                                  <div
                                    className="bg-primary bg-opacity-10 text-primary rounded-circle d-flex align-items-center justify-content-center me-3"
                                    style={{
                                      width: "45px",
                                      height: "45px",
                                      fontSize: "1.2rem",
                                    }}
                                  >
                                    <i className="fas fa-paper-plane"></i>
                                  </div>
                                  <div>
                                    <h6 className="mb-1 fw-bold text-dark">
                                      Trình hồ sơ lên Hội đồng
                                    </h6>
                                    <small className="text-muted">
                                      <i className="far fa-clock me-1"></i>{" "}
                                      {formatDateTime(item.createdAt)}
                                    </small>
                                  </div>
                                </div>
                                <div className="bg-light rounded-3 p-3 mb-4 border-start border-primary border-3">
                                  <span className="fw-bold text-secondary d-block mb-2">
                                    Báo cáo / Nhận xét đính kèm:
                                  </span>
                                  <p className="mb-0 text-dark fst-italic lh-base">
                                    "{item.note || "Không có báo cáo chi tiết."}
                                    "
                                  </p>
                                </div>
                                <div>
                                  <span className="fw-bold text-secondary d-block mb-3 border-bottom pb-2">
                                    <i className="fas fa-folder-open me-2"></i>{" "}
                                    Hồ sơ / Báo cáo Biên tập nộp lúc đó:
                                  </span>
                                  {item.files?.length > 0 ? (
                                    <div className="bg-light p-2 rounded border">
                                      <SeriesFileList files={item.files} />
                                    </div>
                                  ) : (
                                    <p className="text-muted fst-italic small mb-0 px-2">
                                      Không tìm thấy tệp đính kèm nào ở vòng
                                      này.
                                    </p>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-center py-5 bg-light rounded-3 border border-light">
                          <i className="fas fa-inbox fs-2 text-muted opacity-25 mb-3 d-block"></i>
                          <p className="text-muted fst-italic mb-0">
                            Chưa có lần nào bạn trình hồ sơ này lên Hội đồng.
                          </p>
                        </div>
                      )}
                    </div>
                  )}
                  {historySubTab === "revision" && (
                    <div className="animate-fade-in mt-2">
                      {workflowHistory.requestRevision?.length > 0 ? (
                        <div>
                          {workflowHistory.requestRevision.map((item, idx) => (
                            <div
                              key={idx}
                              className="card mb-4 border border-light shadow-sm rounded-3"
                            >
                              <div className="card-body p-4">
                                <div className="d-flex align-items-center mb-3 pb-3 border-bottom">
                                  <div
                                    className="bg-warning bg-opacity-10 text-warning rounded-circle d-flex align-items-center justify-content-center me-3"
                                    style={{
                                      width: "45px",
                                      height: "45px",
                                      fontSize: "1.2rem",
                                    }}
                                  >
                                    <i className="fas fa-edit"></i>
                                  </div>
                                  <div>
                                    <h6 className="mb-1 fw-bold text-dark">
                                      Yêu cầu Mangaka chỉnh sửa toàn bộ Series
                                    </h6>
                                    <small className="text-muted">
                                      <i className="far fa-clock me-1"></i>{" "}
                                      {formatDateTime(item.createdAt)}
                                    </small>
                                  </div>
                                </div>
                                <div className="bg-light rounded-3 p-3 mb-4 border-start border-warning border-3">
                                  <span className="fw-bold text-secondary d-block mb-2">
                                    Nội dung yêu cầu từ bạn:
                                  </span>
                                  <p className="mb-0 text-dark fst-italic lh-base">
                                    "{item.note || "Không có ghi chú cụ thể."}"
                                  </p>
                                </div>
                                <div>
                                  <span className="fw-bold text-secondary d-block mb-3 border-bottom pb-2">
                                    <i className="fas fa-folder-open me-2"></i>{" "}
                                    Bản thảo Mangaka nộp lúc đó:
                                  </span>
                                  {item.files?.length > 0 ? (
                                    <div className="bg-light p-2 rounded border">
                                      <SeriesFileList files={item.files} />
                                    </div>
                                  ) : (
                                    <p className="text-muted fst-italic small mb-0 px-2">
                                      Không tìm thấy tệp đính kèm nào ở vòng
                                      này.
                                    </p>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <div className="text-center py-5 bg-light rounded-3 border border-light">
                          <i className="fas fa-inbox fs-2 text-muted opacity-25 mb-3 d-block"></i>
                          <p className="text-muted fst-italic mb-0">
                            Bạn chưa từng yêu cầu Mangaka sửa tác phẩm này.
                          </p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
