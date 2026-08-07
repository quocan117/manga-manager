import React, { useState, useEffect } from "react";
import { getSeriesArchiveHistory } from "../services/seriesService";
import SeriesFileList from "./SeriesFileList";
import { formatDateTime } from "../utils/formatDate";

export default function DossierHistoryTimeline({ seriesId }) {
  const [historyRounds, setHistoryRounds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (seriesId) {
      setLoading(true);
      getSeriesArchiveHistory(seriesId)
        .then((data) => setHistoryRounds(data || []))
        .catch((err) =>
          setError("Không thể tải lịch sử hồ sơ. Vui lòng thử lại sau."),
        )
        .finally(() => setLoading(false));
    }
  }, [seriesId]);

  if (loading)
    return <div className="text-center py-4">Đang tải lịch sử hồ sơ...</div>;
  if (error) return <div className="alert alert-danger">{error}</div>;
  if (historyRounds.length === 0)
    return (
      <div className="text-muted fst-italic py-3">
        Chưa có lịch sử nộp/duyệt nào cho hồ sơ này.
      </div>
    );

  return (
    <div className="dossier-timeline">
      {historyRounds.map((round, index) => {
        const isInternalRejection = round.decision === "EDITOR_REJECTED_SERIES";
        const showDecision = round.decision && !isInternalRejection;

        return (
          <div
            key={index}
            className="card mb-4 shadow-sm border-0"
            style={{ borderLeft: "4px solid #6c757d" }}
          >
            <div className="card-header bg-light d-flex justify-content-between align-items-center">
              <small className="text-muted">
                Nộp lúc: {formatDateTime(round.submittedAt)}
              </small>
            </div>
            <div className="card-body">
              <h6 className="fw-bold mb-2">Tài liệu đã nộp:</h6>
              <div className="mb-3">
                <SeriesFileList
                  files={round.submittedFiles || []}
                  emptyText="Không có tài liệu nào được đính kèm."
                />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
