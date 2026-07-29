import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getMyTasks,
  getNotifications,
} from "../../../services/assistantService";
import "../../mangaka/styles/DashboardMangaka.css";

const TASK_STATUS_META = {
  ASSIGNED: { label: "Đang xử lý", color: "#60a5fa" },
  SUBMITTED: { label: "Đã nộp", color: "#22c55e" },
  REVISION_REQUESTED: { label: "Yêu cầu chỉnh sửa", color: "#f97316" },
  APPROVED: { label: "Đã duyệt", color: "#a78bfa" },
};

export default function DashboardAssistant() {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [taskData, notifData] = await Promise.all([
        getMyTasks(),
        getNotifications(),
      ]);
      setTasks(taskData || []);
      setUnreadCount((notifData || []).filter((n) => !n.isRead).length);
    } catch (error) {
      console.error("Lỗi khi tải dashboard:", error);
    } finally {
      setLoading(false);
    }
  };

  const pendingCount = tasks.filter((t) => t.status === "ASSIGNED").length;
  const submittedCount = tasks.filter((t) => t.status === "SUBMITTED").length;

  const recentTasks = useMemo(
    () =>
      tasks
        .filter(
          (t) => t.status === "ASSIGNED" || t.status === "REVISION_REQUESTED",
        )
        .slice(0, 5),
    [tasks],
  );

  return (
    <div>
      <h2 className="mb-4">Tổng quan</h2>

      <div className="row g-3 mb-4">
        <div className="col-md-3 col-sm-6">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-total">📋</div>
            <div>
              <div className="kpi-label">Tổng công việc</div>
              <div className="kpi-value">{tasks.length}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3 col-sm-6">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-pending">⏳</div>
            <div>
              <div className="kpi-label">Đang xử lý</div>
              <div className="kpi-value">{pendingCount}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3 col-sm-6">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-published">📤</div>
            <div>
              <div className="kpi-label">Đã nộp</div>
              <div className="kpi-value">{submittedCount}</div>
            </div>
          </div>
        </div>
        <div className="col-md-3 col-sm-6">
          <div className="kpi-card">
            <div className="kpi-icon kpi-icon-alert">🔔</div>
            <div>
              <div className="kpi-label">Thông báo chưa đọc</div>
              <div className="kpi-value">{unreadCount}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="row g-3">
        <div>
          <div className="chart-card h-100">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <span className="chart-card-title mb-0">Việc cần xử lý</span>
              <button
                className="btn btn-sm btn-outline-primary"
                onClick={() => navigate("/assistant/tasks")}
              >
                Xem tất cả
              </button>
            </div>

            {loading ? (
              <div className="text-center text-muted py-4">Đang tải...</div>
            ) : recentTasks.length === 0 ? (
              <div className="empty-text text-center py-4">
                Không có công việc cần xử lý.
              </div>
            ) : (
              <ul className="ranking-list">
                {recentTasks.map((task) => {
                  const meta = TASK_STATUS_META[task.status];
                  return (
                    <li
                      key={task.id}
                      className="ranking-item"
                      style={{
                        justifyContent: "space-between",
                        cursor: "pointer",
                      }}
                      onClick={() => navigate(`/assistant/tasks/${task.id}`)}
                    >
                      <span className="ranking-title">
                        {task.title}
                        {task.seriesTitle && (
                          <span className="text-muted">
                            {" "}
                            · Chapter {task.chapterNumber} - {task.seriesTitle}
                          </span>
                        )}
                      </span>
                      <span
                        className="status-badge"
                        style={{ backgroundColor: meta?.color || "#9ca3af" }}
                      >
                        {meta?.label || task.status}
                      </span>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
