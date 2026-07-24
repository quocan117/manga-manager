import React, { useState, useEffect } from "react";
import {
  getPublishSchedules,
  createPublishSchedule, 
  updatePublishSchedule, 
} from "../../../services/boardService";
import { getApprovedSeries } from "../../../services/boardService"; 
import { formatDateTime } from "../../../utils/formatDate";

export default function PublishScheduleView() {
  const [schedules, setSchedules] = useState([]);
  const [approvedSeries, setApprovedSeries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    seriesId: "",
    publishDate: "",
    frequency: "WEEKLY",
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [schData, seriesData] = await Promise.all([
        getPublishSchedules(),
        getApprovedSeries(),
      ]);
      setSchedules(schData);
      setApprovedSeries(seriesData);
    } catch (error) {
      console.error("Lỗi tải lịch xuất bản:", error);
    } finally {
      setLoading(false);
    }
  };

  const myCoordinatedSeries = approvedSeries.filter(
    (s) => s.isPublicationCoordinator,
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createPublishSchedule({
        seriesId: Number(form.seriesId),
        publishDate:
          form.publishDate.length === 16
            ? `${form.publishDate}:00`
            : form.publishDate,
        frequency: form.frequency,
        status: "PLANNED",
      });
      alert("Đã chốt lịch xuất bản!");
      fetchData();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Bạn không có quyền đặt lịch cho series này.",
      );
    }
  };

  if (loading)
    return (
      <div className="tab-content">
        <h2>Đang tải lịch xuất bản...</h2>
      </div>
    );

  return (
    <div className="tab-content">
      <h2>📅 Lịch Xuất Bản</h2>
      {myCoordinatedSeries.length > 0 && (
        <div className="card shadow-sm border-0 mb-4">
          <div className="card-header bg-white fw-bold">
            Đặt lịch cho Series bạn là Publication Coordinator
          </div>
          <div className="card-body">
            <form onSubmit={handleSubmit} className="row g-3">
              <div className="col-md-4">
                <select
                  className="form-select"
                  value={form.seriesId}
                  onChange={(e) =>
                    setForm({ ...form, seriesId: e.target.value })
                  }
                  required
                >
                  <option value="">-- Chọn Series --</option>
                  {myCoordinatedSeries.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.title}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-4">
                <input
                  type="datetime-local"
                  className="form-control"
                  value={form.publishDate}
                  onChange={(e) =>
                    setForm({ ...form, publishDate: e.target.value })
                  }
                  required
                />
              </div>
              <div className="col-md-3">
                <select
                  className="form-select"
                  value={form.frequency}
                  onChange={(e) =>
                    setForm({ ...form, frequency: e.target.value })
                  }
                >
                  <option value="WEEKLY">Hàng Tuần</option>
                  <option value="MONTHLY">Hàng Tháng</option>
                </select>
              </div>
              <div className="col-md-1">
                <button type="submit" className="btn btn-primary w-100">
                  Lưu
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      <p className="text-muted">
        Việc chốt lịch chỉ do Publication Coordinator của từng Series thực hiện.
        Các thành viên khác chỉ xem danh sách bên dưới.
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
