import React, { useState, useEffect } from "react";
import {
  getStudioProgress,
  getPendingReviewSeries,
} from "../../../services/tantouService";
import api from "../../../services/api";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import "../styles/TantouEditor.css";

export default function TantouHistoryPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [selectedSeriesId, setSelectedSeriesId] = useState("");
  const [workflowHistory, setWorkflowHistory] = useState({
    submitToBoard: [],
    requestRevision: [],
  });
  const [historySubTab, setHistorySubTab] = useState("submit");
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);

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
      return;
    }
    const fetchHistory = async () => {
      setHistoryLoading(true);
      try {
        const res = await api.get(
          `/api/series/${selectedSeriesId}/editor-workflow-history`,
        );
        setWorkflowHistory(
          res.data || { submitToBoard: [], requestRevision: [] },
        );
      } catch (err) {
        console.error("Lỗi tải lịch sử:", err);
      } finally {
        setHistoryLoading(false);
      }
    };
    fetchHistory();
  }, [selectedSeriesId]);

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
            <div className="card-body">
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
              <div className="mt-4 text-muted small fst-italic">
                * Chỉ hiển thị các Series mà bạn đang trực tiếp phụ trách hoặc
                đang nằm trong danh sách cần kiểm duyệt.
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
                                    📝 Báo cáo / Nhận xét đính kèm:
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
                                      Yêu cầu Mangaka chỉnh sửa
                                    </h6>
                                    <small className="text-muted">
                                      <i className="far fa-clock me-1"></i>{" "}
                                      {formatDateTime(item.createdAt)}
                                    </small>
                                  </div>
                                </div>

                                <div className="bg-light rounded-3 p-3 mb-4 border-start border-warning border-3">
                                  <span className="fw-bold text-secondary d-block mb-2">
                                    💬 Nội dung yêu cầu từ bạn:
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
