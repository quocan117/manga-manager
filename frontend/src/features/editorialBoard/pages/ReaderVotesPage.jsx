import React, { useState, useEffect } from "react";
import {
  getMyAssignedSeries,
  importReaderFeedback,
} from "../../../services/boardService";
import { toBackendDateTime } from "../../../utils/formatDate";

export default function ReaderVotesPage() {
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [assignedSeries, setAssignedSeries] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedSeriesId, setSelectedSeriesId] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchAssigned = async () => {
      try {
        const data = await getMyAssignedSeries();
        setAssignedSeries(data || []);
      } catch (err) {
        console.error("Lỗi tải danh sách series được phân công:", err);
      }
    };
    fetchAssigned();
  }, []);

  const filteredSeries = assignedSeries.filter((s) =>
    s.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const handleImport = async () => {
    if (!from || !to) return alert("Vui lòng chọn khoảng thời gian tổng hợp.");
    if (!selectedSeriesId)
      return alert("Vui lòng chọn một Series từ danh sách bên trên.");

    if (
      !window.confirm(
        "Xác nhận tổng hợp dữ liệu bình chọn cho khoảng thời gian này?",
      )
    )
      return;

    try {
      setLoading(true);
      const fromParam = toBackendDateTime(from);
      const toParam = toBackendDateTime(to);

      await importReaderFeedback(selectedSeriesId, fromParam, toParam);
      alert("Đã tổng hợp dữ liệu thành công!");
      setSelectedSeriesId("");
    } catch (err) {
      alert(
        err?.response?.data?.message ||
          "Lỗi khi tổng hợp dữ liệu. Backend đã chặn nếu bạn không có quyền xử lý Series này.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="tab-content">
      <h2>🗳️ Nhập Dữ Liệu Bình Chọn</h2>
      <p className="text-muted">
        Tổng hợp lượt vote theo khoảng thời gian cho các Series bạn phụ trách.
      </p>

      <div className="card shadow-sm mb-4">
        <div className="card-body">
          <div className="row g-3">
            <div className="col-md-6">
              <label className="form-label fw-bold">Từ thời điểm</label>
              <input
                type="datetime-local"
                className="form-control"
                value={from}
                onChange={(e) => setFrom(e.target.value)}
              />
            </div>
            <div className="col-md-6">
              <label className="form-label fw-bold">Đến thời điểm</label>
              <input
                type="datetime-local"
                className="form-control"
                value={to}
                onChange={(e) => setTo(e.target.value)}
              />
            </div>
          </div>
        </div>
      </div>

      <div className="card shadow-sm mb-4">
        <div className="card-header bg-white fw-bold">
          Chọn Series Phụ Trách
        </div>
        <div className="card-body">
          <input
            type="text"
            className="form-control mb-3"
            placeholder="🔍 Tìm kiếm Series..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />

          <div
            className="list-group mb-4"
            style={{ maxHeight: "250px", overflowY: "auto" }}
          >
            {filteredSeries.length === 0 && (
              <div className="list-group-item text-muted text-center py-4">
                Không tìm thấy Series phù hợp.
              </div>
            )}
            {filteredSeries.map((s) => {
              const sId = s.id || s.seriesId;
              return (
                <button
                  key={sId}
                  type="button"
                  className={`list-group-item list-group-item-action d-flex justify-content-between align-items-center ${
                    selectedSeriesId === sId ? "active" : ""
                  }`}
                  onClick={() => setSelectedSeriesId(sId)}
                >
                  <span>
                    <strong>#{sId}</strong> - {s.title}
                  </span>
                  {selectedSeriesId === sId && (
                    <span className="badge bg-light text-primary">
                      Đang chọn
                    </span>
                  )}
                </button>
              );
            })}
          </div>

          <button
            className="btn btn-primary w-100 fw-bold"
            onClick={handleImport}
            disabled={loading}
          >
            {loading ? "Đang xử lý..." : "Tổng hợp dữ liệu cho Series này"}
          </button>
        </div>
      </div>
    </div>
  );
}
