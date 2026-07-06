import { useEffect, useState } from "react";
import { getMyTasks, getNotifications } from "../../services/assistantService";
export default function DashboardAssistant() {
  const [tasks, setTasks] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
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
    }
  };
  const pendingCount = tasks.filter((t) => t.status === "ASSIGNED").length;
  const submittedCount = tasks.filter((t) => t.status === "SUBMITTED").length;
  return (
    <div>
      <h2 className="mb-4">Assistant Dashboard</h2>
      <div className="row">
        <div className="col-md-3">
          <div className="card shadow">
            <div className="card-body text-center">
              <h6>Total Tasks</h6>
              <h2>{tasks.length}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow">
            <div className="card-body text-center">
              <h6>Pending Tasks</h6>
              <h2>{pendingCount}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow">
            <div className="card-body text-center">
              <h6>Submitted</h6>
              <h2>{submittedCount}</h2>
            </div>
          </div>
        </div>
        <div className="col-md-3">
          <div className="card shadow">
            <div className="card-body text-center">
              <h6>Thông báo chưa đọc</h6>
              <h2>{unreadCount}</h2>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}