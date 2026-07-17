import React, { useState, useEffect } from "react";
import { getSchedules, createSchedule } from "../../../services/tantouService";
import { getAllSeries } from "../../../services/seriesService";
import { formatDateTime } from "../../../utils/formatDate";

export default function ScheduleManagement() {
  const [schedules, setSchedules] = useState([]);
  const [publishedSeries, setPublishedSeries] = useState([]);
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
      const [schData, seriesData] = await Promise.all([
        getSchedules(),
        getAllSeries(),
      ]);
      setSchedules(schData);
      setPublishedSeries(
        seriesData.filter(
          (s) => s.status === "Published" || s.status === "PUBLISHED",
        ),
      );
    } catch (error) {
      console.error("Lỗi lấy dữ liệu lịch:", error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createSchedule({
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
      alert("Lỗi lưu lịch.");
    }
  };
  
  return (
    <div className="p-4 bg-light min-vh-100">
      <h2 className="mb-4">📅 Cài Đặt Lịch Phát Hành Bản In/Online</h2>
      <div className="row">
        <div className="col-md-4">
          <div className="card shadow-sm border-0 mb-4">
            <div className="card-header bg-white fw-bold">
              Chốt Deadline Xuất Bản Mới
            </div>
            <div className="card-body">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label">
                    Chọn Series (Đã qua Hội đồng)
                  </label>
                  <select
                    className="form-select"
                    value={form.seriesId}
                    onChange={(e) =>
                      setForm({ ...form, seriesId: e.target.value })
                    }
                    required
                  >
                    <option value="">-- Chọn Series --</option>
                    {publishedSeries.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.title}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Ngày xuất bản Chapter 1</label>
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
                <div className="mb-4">
                  <label className="form-label">Chu kỳ phát hành</label>
                  <select
                    className="form-select"
                    value={form.frequency}
                    onChange={(e) =>
                      setForm({ ...form, frequency: e.target.value })
                    }
                  >
                    <option value="WEEKLY">Hàng Tuần (7 ngày)</option>
                    <option value="MONTHLY">Hàng Tháng (30 ngày)</option>
                  </select>
                </div>
                <button type="submit" className="btn btn-primary w-100 fw-bold">
                  Lưu Cài Đặt
                </button>
              </form>
            </div>
          </div>
        </div>
        <div className="col-md-8">
          <div className="card shadow-sm border-0">
            <div className="card-header bg-white fw-bold">
              Danh Sách Lịch Phát Hành
            </div>
            <div className="card-body p-0">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th>Series</th>
                    <th>Ngày Xuất Bản</th>
                    <th>Tần suất</th>
                    <th>Trạng Thái</th>
                  </tr>
                </thead>
                <tbody>
                  {schedules.map((s) => (
                    <tr key={s.id}>
                      <td className="fw-bold">{s.seriesTitle}</td>
                      <td className="text-danger fw-bold">
                        {formatDateTime(s.publishDate)}
                      </td>
                      <td>
                        <span className="badge bg-secondary">
                          {s.frequency}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            s.isOverdue
                              ? "bg-danger"
                              : s.status === "PLANNED"
                                ? "bg-warning text-dark"
                                : "bg-success"
                          }`}
                        >
                          {s.isOverdue ? "⚠️ QUÁ HẠN" : s.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}