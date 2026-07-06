import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getMyTasks } from "../../services/assistantService";
export default function MyTasks() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  useEffect(() => {
    loadTasks();
  }, []);
  const loadTasks = async () => {
    try {
      const data = await getMyTasks();
      setTasks(data || []);
    } catch (error) {
      console.error("Lỗi khi tải danh sách nhiệm vụ:", error);
    } finally {
      setLoading(false);
    }
  };
  if (loading) return <div className="text-center mt-4">Đang tải...</div>;
  return (
    <div>
      <h2>My Tasks</h2>
      <div className="row">
        {tasks.length === 0 && (
          <div className="col-12 text-muted text-center">
            Chưa có nhiệm vụ nào.
          </div>
        )}
        {tasks.map((task) => (
          <div key={task.id} className="col-md-6 mb-3">
            <div className="card shadow">
              <div className="card-body">
                <h5>{task.title}</h5>
                <p className="mb-1 text-muted">{task.description}</p>
                <p className="mb-1 small">
                  Chapter {task.chapterNumber} - Trang {task.pageNumber} (
                  {task.seriesTitle})
                </p>
                <span className="badge bg-primary mb-2">{task.status}</span>
                <br />
                <button
                  className="btn btn-outline-primary w-100 mt-2"
                  onClick={() => navigate(`/assistant/tasks/${task.id}`)}
                >
                  Xem chi tiết
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}