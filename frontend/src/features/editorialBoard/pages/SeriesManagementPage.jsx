import React, { useEffect, useState } from "react";
import {
  getApprovedSeries,
  getSeriesChapters,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList"; 

export default function SeriesManagementPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [expandedId, setExpandedId] = useState(null);
  const [chaptersBySeries, setChaptersBySeries] = useState({});
  const [loading, setLoading] = useState(true);

  const [viewDossierSeries, setViewDossierSeries] = useState(null);

  useEffect(() => {
    getApprovedSeries()
      .then(setSeriesList)
      .catch((err) => console.error("Lỗi tải series đã duyệt:", err))
      .finally(() => setLoading(false));
  }, []);

  const toggleExpand = async (seriesId) => {
    if (expandedId === seriesId) {
      setExpandedId(null);
      return;
    }
    setExpandedId(seriesId);
    if (!chaptersBySeries[seriesId]) {
      const chapters = await getSeriesChapters(seriesId);
      setChaptersBySeries((prev) => ({ ...prev, [seriesId]: chapters }));
    }
  };

  if (loading)
    return (
      <div className="tab-content">
        <h2>Đang tải...</h2>
      </div>
    );

  return (
    <div className="tab-content">
      <h2 className="mb-4">📚 Quản Lý Series Đã Duyệt</h2>
      <div className="table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Series</th>
              <th>Trạng Thái</th>
              <th>Publication Coordinator</th>
              <th>Ban Phụ Trách</th>
              <th>Tiến Độ</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {seriesList.map((s) => (
              <React.Fragment key={s.id}>
                <tr>
                  <td>
                    <strong>{s.title}</strong>
                  </td>
                  <td>
                    <span className="badge bg-info">{s.status}</span>
                  </td>
                  <td>{s.publicationCoordinatorName || "Chưa có"}</td>
                  <td style={{ fontSize: "0.85rem" }}>
                    {s.boardPanel?.map((m) => (
                      <div key={m.boardMemberId}>{m.boardMemberName}</div>
                    ))}
                  </td>
                  <td>
                    {s.chapterCount} chapter · {s.progress}%
                  </td>
                  <td>
                    <div className="d-flex gap-2">
                      <button
                        className="btn btn-outline-info btn-sm"
                        onClick={() => setViewDossierSeries(s)}
                      >
                        Xem Hồ Sơ
                      </button>
                      <button
                        className="btn btn-outline-primary btn-sm"
                        onClick={() => toggleExpand(s.id)}
                      >
                        {expandedId === s.id ? "Ẩn Chapter" : "Xem Chapter"}
                      </button>
                    </div>
                  </td>
                </tr>
                {expandedId === s.id && (
                  <tr>
                    <td colSpan={6}>
                      <table className="admin-table mb-0">
                        <thead>
                          <tr>
                            <th>Chapter</th>
                            <th>Trạng Thái</th>
                            <th>Ngày Xuất Bản</th>
                          </tr>
                        </thead>
                        <tbody>
                          {(chaptersBySeries[s.id] || []).map((c) => (
                            <tr key={c.id}>
                              <td>
                                #{c.chapterNumber} {c.title}
                              </td>
                              <td>{c.status}</td>
                              <td>
                                {c.releaseDate
                                  ? formatDateTime(c.releaseDate)
                                  : "-"}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </td>
                  </tr>
                )}
              </React.Fragment>
            ))}
          </tbody>
        </table>
      </div>

      {viewDossierSeries && (
        <div
          className="custom-modal-overlay"
          onClick={() => setViewDossierSeries(null)}
        >
          <div
            className="custom-modal-content series-review-preview-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              className="close-btn"
              onClick={() => setViewDossierSeries(null)}
              aria-label="Đóng"
            >
              ✕
            </button>
            <h4 className="mb-1">Hồ sơ lưu trữ: {viewDossierSeries.title}</h4>
            <p className="text-muted mb-3 fst-italic">
              Đây là các tài liệu đã được Hội đồng Biên tập phê duyệt ở vòng
              duyệt cuối cùng.
            </p>
            <SeriesFileList
              files={viewDossierSeries.uploadedFiles || []}
              emptyText="Không có tệp hồ sơ đính kèm."
            />
          </div>
        </div>
      )}
    </div>
  );
}
