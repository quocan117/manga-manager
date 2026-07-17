import React, { useState, useEffect } from "react";
import {
  getAssistants,
  createAssistant,
  updateAssistantStatus,
  deleteAssistant,
} from "../../../services/mangakaService";
import "../../editorialBoard/styles/EditorialBoard.css";
import { formatDateOnly } from "../../../utils/formatDate";

export default function ManageAssistants() {
  const [assistants, setAssistants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });

  useEffect(() => {
    fetchAssistants();
  }, []);

  const fetchAssistants = async () => {
    try {
      const data = await getAssistants();
      setAssistants(data);
    } catch (error) {
      console.error("Lỗi khi tải danh sách trợ lý:", error);
    }
  };

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password.length < 8) {
      alert("Mật khẩu phải có ít nhất 8 ký tự!");
      return;
    }
    try {
      setLoading(true);
      await createAssistant(form);
      alert("Tạo tài khoản trợ lý thành công!");
      setForm({ username: "", email: "", password: "" });
      fetchAssistants();
    } catch (error) {
      console.error(error);
      if (error.response && error.response.status === 409) {
        alert("Lỗi: Username hoặc Email đã tồn tại trong hệ thống!");
      } else {
        alert("Lỗi máy chủ: Không thể tạo tài khoản lúc này.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id, currentStatus) => {
    const newStatus = currentStatus === "ACTIVE" ? "INACTIVE" : "ACTIVE";
    const actionText = newStatus === "ACTIVE" ? "MỞ KHÓA" : "KHÓA";
    if (
      window.confirm(
        `Bạn có chắc chắn muốn ${actionText} tài khoản trợ lý này?`,
      )
    ) {
      try {
        await updateAssistantStatus(id, newStatus);
        alert(`Đã ${actionText} tài khoản thành công!`);
        setAssistants((prev) =>
          prev.map((a) => (a.id === id ? { ...a, status: newStatus } : a)),
        );
      } catch (error) {
        if (error.response && error.response.status === 403) {
          alert("Bạn chỉ có thể quản lý trợ lý do chính bạn tạo.");
        } else {
          alert("Không thể thay đổi trạng thái lúc này.");
        }
      }
    }
  };

  const handleDelete = async (id, username) => {
    if (
      window.confirm(
        `CẢNH BÁO: Bạn có chắc muốn XÓA tài khoản trợ lý "${username}"?`,
      )
    ) {
      try {
        await deleteAssistant(id);
        alert(`Đã xóa tài khoản "${username}".`);
        setAssistants((prev) =>
          prev.map((a) => (a.id === id ? { ...a, status: "DELETED" } : a)),
        );
      } catch (error) {
        if (error.response && error.response.status === 403) {
          alert("Bạn chỉ có thể quản lý trợ lý do chính bạn tạo.");
        } else {
          alert("Lỗi kết nối mạng hoặc hệ thống từ chối.");
        }
      }
    }
  };
  
  return (
    <div className="tab-content" style={{ padding: "20px" }}>
      <h2 className="mb-4">👤 Quản Lý & Cấp Tài Khoản Trợ Lý</h2>
      <div className="card shadow mb-5" style={{ maxWidth: "600px" }}>
        <div className="card-header bg-primary text-white">
          Khởi Tạo Tài Khoản Trợ Lý Mới
        </div>
        <div className="card-body">
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label">Tên đăng nhập (Username)</label>
              <input
                type="text"
                name="username"
                className="form-control"
                value={form.username}
                onChange={handleChange}
                required
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Email liên hệ</label>
              <input
                type="email"
                name="email"
                className="form-control"
                value={form.email}
                onChange={handleChange}
                required
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Loại tài khoản</label>
              <select className="form-select" value="ASSISTANT" disabled>
                <option value="ASSISTANT">Trợ lý</option>
              </select>
              <div className="form-text">
                Tài khoản trợ lý sẽ luôn thuộc quyền quản lý của bạn (Mangaka
                hiện tại).
              </div>
            </div>
            <div className="mb-4">
              <label className="form-label">Mật khẩu cấp phát</label>
              <input
                type="password"
                name="password"
                className="form-control"
                value={form.password}
                onChange={handleChange}
                placeholder="Tối thiểu 8 ký tự"
                required
                minLength={8}
              />
            </div>
            <button
              type="submit"
              className="btn btn-success w-100"
              disabled={loading}
            >
              {loading ? "Đang xử lý..." : "Cấp Tài Khoản"}
            </button>
          </form>
        </div>
      </div>
      <h4 className="mb-3">Danh sách Trợ lý của bạn</h4>
      <div className="card shadow">
        <div className="card-body p-0">
          <table className="table table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>STT</th>
                <th>Username</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Ngày tham gia</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {assistants.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-4 text-muted">
                    Bạn chưa có trợ lý nào.
                  </td>
                </tr>
              ) : (
                assistants.map((ast, index) => (
                  <tr key={ast.id || index}>
                    <td>#{index + 1}</td>
                    <td>
                      <strong>{ast.username}</strong>
                    </td>
                    <td>{ast.email}</td>
                    <td>
                      <span className="badge role-assistant">Trợ Lý</span>
                    </td>
                    <td>
                      <span
                        className={`badge ${
                          ast.status === "ACTIVE"
                            ? "bg-success"
                            : ast.status === "DELETED"
                              ? "bg-danger"
                              : "bg-warning"
                        }`}
                      >
                        {ast.status}
                      </span>
                    </td>
                    <td>
                      {ast.createdAt
                        ? formatDateOnly(ast.createdAt)
                        : "—"}
                    </td>
                    <td>
                      <div className="d-flex gap-2 align-items-center">
                        <button
                          className={`btn btn-sm me-2 ${ast.status === "ACTIVE" ? "btn-outline-warning" : "btn-outline-success"}`}
                          onClick={() => handleToggleStatus(ast.id, ast.status)}
                          disabled={ast.status === "DELETED"}
                        >
                          {ast.status === "ACTIVE" ? "Khóa" : "Mở khóa"}
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDelete(ast.id, ast.username)}
                          disabled={ast.status === "DELETED"}
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}