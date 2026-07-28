import React, { useState, useEffect } from "react";
import {
  getApprovedSeries,
  getSeriesFeedbackHistory,
} from "../../../services/boardService";
import { formatDateTime } from "../../../utils/formatDate";

export default function RankingDecisionPage() {
  const [seriesList, setSeriesList] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedSeriesId, setSelectedSeriesId] = useState("");
  const [historyPeriods, setHistoryPeriods] = useState([]);
  const [selectedHistoryId, setSelectedHistoryId] = useState("");

  useEffect(() => {
    const fetchSeries = async () => {
      try {
        const data = await getApprovedSeries();
        setSeriesList(data || []);
      } catch (error) {
        console.error("Lỗi tải danh sách Series:", error);
      }
    };
    fetchSeries();
  }, []);

  useEffect(() => {
    if (!selectedSeriesId) {
      setHistoryPeriods([]);
      setSelectedHistoryId("");
      return;
    }
    const fetchHistory = async () => {
      try {
        const data = await getSeriesFeedbackHistory(selectedSeriesId);
        setHistoryPeriods(data || []);
        if (data && data.length > 0) setSelectedHistoryId(data[0].importId);
      } catch (error) {
        console.error("Lỗi tải lịch sử tổng hợp:", error);
      }
    };
    fetchHistory();
  }, [selectedSeriesId]);

  const filteredSeries = seriesList.filter((s) =>
    s.title.toLowerCase().includes(searchQuery.toLowerCase()),
  );

  const selectedData = historyPeriods.find(
    (h) => h.importId.toString() === selectedHistoryId.toString(),
  );

  return (
    <div className="tab-content">
      <h2>📊 Thống Kê Số Liệu Series</h2>
      <p className="text-muted">
        Tra cứu lịch sử tổng hợp lượt bình chọn theo từng Series cụ thể.
      </p>

      <div className="row mb-4">
        <div className="col-md-6">
          <label className="form-label fw-bold">1. Tìm & Chọn Series</label>
          <input
            type="text"
            className="form-control mb-2"
            placeholder="🔍 Tìm tên Series..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          <select
            className="form-select"
            value={selectedSeriesId}
            onChange={(e) => setSelectedSeriesId(e.target.value)}
          >
            <option value="">-- Chọn Series --</option>
            {filteredSeries.map((s) => (
              <option key={s.id} value={s.id}>
                {s.title}
              </option>
            ))}
          </select>
        </div>

        <div className="col-md-6">
          <label className="form-label fw-bold">
            2. Lần tổng hợp theo thời gian
          </label>
          <select
            className="form-select"
            value={selectedHistoryId}
            onChange={(e) => setSelectedHistoryId(e.target.value)}
            disabled={!selectedSeriesId || historyPeriods.length === 0}
          >
            {historyPeriods.length === 0 && (
              <option value="">Chưa có dữ liệu tổng hợp nào</option>
            )}
            {historyPeriods.map((h) => (
              <option key={h.importId} value={h.importId}>
                {formatDateTime(h.periodStart)} ➔ {formatDateTime(h.periodEnd)}
              </option>
            ))}
          </select>
        </div>
      </div>

      {selectedData && (
        <div className="card shadow-sm border-0 border-start border-4 border-primary">
          <div className="card-body">
            <h5 className="text-primary mb-3">Kết quả thống kê</h5>
            <div className="row text-center">
              <div className="col-6 border-end">
                <p className="text-muted mb-1">Thời gian tổng hợp</p>
                <strong>
                  {formatDateTime(selectedData.periodStart)} -{" "}
                  {formatDateTime(selectedData.periodEnd)}
                </strong>
              </div>
              <div className="col-6">
                <p className="text-muted mb-1">Tổng số lượt Vote nhận được</p>
                <h3 className="text-danger mb-0">
                  {selectedData.voteCount.toLocaleString()}
                </h3>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
