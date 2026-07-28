import { useEffect, useState } from "react";
import {
  getMyTasks,
  getTaskSubmissions,
} from "../../../services/assistantService";
import { formatDateTime } from "../../../utils/formatDate";

export default function MySubmissions() {
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSubmissions();
  }, []);

  const loadSubmissions = async () => {
    try {
      const tasks = await getMyTasks();
      const relevantTasks = tasks.filter((t) =>
        ["SUBMITTED", "APPROVED", "REVISION_REQUESTED"].includes(t.status),
      );
      const results = await Promise.all(
        relevantTasks.map((t) => getTaskSubmissions(t.id)),
      );
      setSubmissions(results.flat());
    } catch (error) {
      console.error("Lỗi khi tải submissions:", error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="text-center mt-4">Đang tải...</div>;
  
  return (
    <div>
      <h2>Bài nộp của tôi</h2>
      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tình trạng</th>
            <th>Xem ghi chú</th>
            <th>Nộp vào</th>
          </tr>
        </thead>
        <tbody>
          {submissions.length === 0 && (
            <tr>
              <td colSpan={4} className="text-center text-muted">
                Chưa có bài nộp nào.
              </td>
            </tr>
          )}
          {submissions.map((item) => (
            <tr key={item.id}>
              <td>{item.id}</td>
              <td>{item.status}</td>
              <td>{item.reviewNote || "-"}</td>
              <td>
                {item.submittedAt
                  ? formatDateTime(item.submittedAt)
                  : "-"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}