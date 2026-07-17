import React, { useMemo, useState } from "react";
import {
  getReaderVoteSummary,
  getReaderVotes,
  getReaderFeedbackImports,
  importReaderFeedback,
} from "../../../services/boardService";
import { formatDateTime, toBackendDateTime } from "../../../utils/formatDate";

export default function ReaderVotesPage() {
  const [period, setPeriod] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [summary, setSummary] = useState([]);
  const [votes, setVotes] = useState([]);
  const [loading, setLoading] = useState(false);

  const readerCodeByToken = useMemo(() => {
    const map = new Map();
    let counter = 0;
    for (const v of votes) {
      const token = v.guestSessionToken;
      if (token && !map.has(token)) {
        counter += 1;
        map.set(token, `Guest-${String(counter).padStart(3, "0")}`);
      }
    }
    return map;
  }, [votes]);

  const getReaderCode = (token) => {
    if (!token) return "—";
    return readerCodeByToken.get(token) || "Guest-???";
  };

  const handlePreview = async () => {
    if (!from || !to) return alert("Vui lòng chọn khoảng thời gian.");
    setLoading(true);
    try {
      const fromParam = toBackendDateTime(from);
      const toParam = toBackendDateTime(to);
      const [summaryData, voteData] = await Promise.all([
        getReaderVoteSummary(fromParam, toParam),
        getReaderVotes(fromParam, toParam),
      ]);
      setSummary(summaryData);
      setVotes(voteData);
    } catch (err) {
      alert("Không tải được dữ liệu bình chọn.");
    } finally {
      setLoading(false);
    }
  };

  const handleImport = async () => {
    const trimmedPeriod = period.trim();
    if (!trimmedPeriod) return alert("Vui lòng đặt tên chu kỳ, ví dụ: 2026-T3");

    try {
      const existingImports = await getReaderFeedbackImports();
      const periodAlreadyExists = (existingImports || []).some(
        (imp) => (imp.period || "").trim() === trimmedPeriod,
      );
      if (periodAlreadyExists) {
        const confirmOverwrite = window.confirm(
          `Kỳ "${trimmedPeriod}" đã có dữ liệu tổng hợp trước đó!\n\n` +
            `Nếu tiếp tục, dữ liệu CŨ của kỳ "${trimmedPeriod}" sẽ bị GHI ĐÈ bằng số liệu mới ` +
            `(không tạo kỳ mới).\n\n` +
            `- Nếu bạn thực sự muốn cập nhật lại kỳ này, bấm OK.\n` +
            `- Nếu bạn đang muốn tổng hợp cho một kỳ MỚI, bấm Hủy và đổi tên chu kỳ ` +
            `(ví dụ đổi "${trimmedPeriod}" thành kỳ tiếp theo) rồi thử lại.`,
        );
        if (!confirmOverwrite) return;
      }
    } catch (err) {
      alert("Lưu ý: Không thể kiểm tra lịch sử trùng kỳ do lỗi kết nối.");
    }

    if (
      !window.confirm(
        `Xác nhận tổng hợp dữ liệu bình chọn cho kỳ "${trimmedPeriod}"? Hệ thống sẽ tự tính bảng xếp hạng.`,
      )
    )
      return;
    try {
      await importReaderFeedback(trimmedPeriod, from, to);
      alert("Đã tổng hợp dữ liệu và cập nhật bảng xếp hạng!");
      setPeriod("");
      setFrom("");
      setTo("");
      setSummary([]);
      setVotes([]);
    } catch (err) {
      alert(err?.response?.data?.message || "Lỗi khi tổng hợp dữ liệu.");
    }
  };

  return (
    <div className="tab-content">
      <h2>🗳️ Bình Chọn Độc Giả Theo Kỳ</h2>
      <p>
        Xem độc giả nào đã like series nào trong kỳ, sau đó tổng hợp thành dữ
        liệu chính thức để hệ thống tự tính bảng xếp hạng.
      </p>
      <div className="board-header" style={{ gap: 10, flexWrap: "wrap" }}>
        <input
          type="text"
          placeholder="Chu kỳ (VD: 2026-T3)"
          value={period}
          onChange={(e) => setPeriod(e.target.value)}
        />
        <input
          type="datetime-local"
          value={from}
          onChange={(e) => setFrom(e.target.value)}
        />
        <input
          type="datetime-local"
          value={to}
          onChange={(e) => setTo(e.target.value)}
        />
        <button
          className="btn-cancel-series"
          onClick={handlePreview}
          disabled={loading}
        >
          Xem dữ liệu
        </button>
        <button
          className="btn btn-primary"
          onClick={handleImport}
          disabled={summary.length === 0}
        >
          Tổng hợp & Tính Xếp Hạng
        </button>
      </div>
      <h3 style={{ marginTop: 20 }}>Tổng số lượt bình chọn theo Series</h3>
      <table className="admin-table">
        <thead>
          <tr>
            <th>Series</th>
            <th>Tổng lượt bình chọn</th>
          </tr>
        </thead>
        <tbody>
          {summary.map((s) => (
            <tr key={s.seriesId}>
              <td>{s.seriesTitle}</td>
              <td>{s.voteCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <h3 style={{ marginTop: 20 }}>Chi tiết độc giả đã bình chọn</h3>
      <table className="admin-table">
        <thead>
          <tr>
            <th>Series</th>
            <th>Chapter</th>
            <th>Mã độc giả</th>
            <th>Thời điểm</th>
          </tr>
        </thead>
        <tbody>
          {votes.map((v) => (
            <tr key={v.likeId}>
              <td>{v.seriesTitle}</td>
              <td>
                Chương {v.chapterNumber} - {v.chapterTitle}
              </td>
              <td>{getReaderCode(v.guestSessionToken)}</td>
              <td>{formatDateTime(v.likedAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
