import React, { useState, useEffect } from "react";
import { getPublishSchedules } from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";

export default function PublishScheduleView() {
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPublishSchedules()
      .then(setSchedules)
      .catch((err) => console.error("Lỗi tải lịch xuất bản:", err))
      .finally(() => setLoading(false));
  }, []);

  if (loading)
    return (
      <div className="tab-content">
        <h2>Đang tải lịch xuất bản...</h2>
      </div>
    );
    
  return (
    <div className="tab-content">
      <h2>📅 Lịch Xuất Bản (Do Biên Tập thiết lập)</h2>
      <p>
        Theo dõi chu kỳ phát hành để nắm tiến độ ra mắt của từng series. Trang
        này chỉ để xem, việc chốt lịch do Tantou Editor thực hiện.
      </p>
      <div className="table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Series</th>
              <th>Ngày Xuất Bản Kế Tiếp</th>
              <th>Chu Kỳ</th>
              <th>Trạng Thái</th>
            </tr>
          </thead>
          <tbody>
            {schedules.map((s) => (
              <tr key={s.id} className={s.isOverdue ? "row-danger" : ""}>
                <td>
                  <strong>{s.seriesTitle}</strong>
                </td>
                <td>{formatDateTime(s.publishDate)}</td>
                <td>
                  <span className="badge bg-secondary">{s.frequency}</span>
                </td>
                <td>{s.isOverdue ? "⚠️ Quá hạn" : s.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}