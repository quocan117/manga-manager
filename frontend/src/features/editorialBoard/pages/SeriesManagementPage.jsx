import React, { useEffect, useState } from "react";
import {
  getApprovedSeries,
  getSeriesChapters,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";
import { useNavigate } from "react-router-dom";

export default function SeriesManagementPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [expandedId, setExpandedId] = useState(null);
  const [chaptersBySeries, setChaptersBySeries] = useState({});
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

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
              <th></th>
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
                    <button
                      className="btn btn-outline-primary btn-sm"
                      onClick={() => toggleExpand(s.id)}
                    >
                      {expandedId === s.id ? "Ẩn" : "Xem Chapter"}
                    </button>
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
                            <th></th>
                          </tr>
                        </thead>
                        <tbody>
                          {(chaptersBySeries[s.id] || []).map((c) => (
                            <React.Fragment key={c.id}>
                              <tr>
                                <td>
                                  #{c.chapterNumber} {c.title}
                                </td>
                                <td>{c.status}</td>
                                <td>
                                  {c.releaseDate
                                    ? formatDateTime(c.releaseDate)
                                    : "-"}
                                </td>
                                <td>
                                  {(c.status === "SUBMITTED_TO_BOARD" ||
                                    c.status === "BOARD_REJECTED" ||
                                    c.status === "APPROVED") && (
                                    <button
                                      className="btn btn-sm btn-success"
                                      onClick={() =>
                                        navigate(
                                          `/board/chapters/${c.id}/review`,
                                        )
                                      }
                                    >
                                      {c.status === "SUBMITTED_TO_BOARD"
                                        ? "Xem & Xác nhận"
                                        : "Xem chi tiết"}
                                    </button>
                                  )}
                                </td>
                              </tr>
                              {c.reviews?.length > 0 && (
                                <tr>
                                  <td
                                    colSpan={4}
                                    style={{ background: "#f8f9fa" }}
                                  >
                                    <div className="d-flex flex-wrap gap-3 py-1">
                                      {c.reviews.map((r) => (
                                        <div
                                          key={r.id}
                                          style={{ fontSize: "0.85rem" }}
                                        >
                                          <strong>
                                            👤 {r.boardMemberName}
                                          </strong>{" "}
                                          {r.confirmed === null ? (
                                            <span className="badge bg-secondary">
                                              Chưa bỏ phiếu
                                            </span>
                                          ) : (
                                            <span
                                              className={`badge ${
                                                r.confirmed
                                                  ? "bg-success"
                                                  : "bg-danger"
                                              }`}
                                            >
                                              {r.confirmed
                                                ? "Đủ điều kiện"
                                                : "Từ chối"}
                                            </span>
                                          )}
                                          {r.comment && (
                                            <div className="text-muted">
                                              Lý do: {r.comment}
                                            </div>
                                          )}
                                        </div>
                                      ))}
                                    </div>
                                  </td>
                                </tr>
                              )}
                            </React.Fragment>
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
    </div>
  );
}
