import React, { useState, useEffect } from "react";
import {
  getUsers,
  createUser,
  updateUserStatus,
  deleteMangaka,
} from "../../../services/boardService";
import "../styles/EditorialBoard.css";
import { formatDateOnly } from "../../../utils/formatDate";

export default function ManageUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    role: "MANGAKA",
  });

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const data = await getUsers();
      const filteredUsers = data.filter(
        (user) => user.role === "MANGAKA" || user.role === "TANTOU_EDITOR",
      );
      setUsers(filteredUsers);
    } catch (error) {
      console.error("Lỗi khi tải danh sách:", error);
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
      await createUser(form);
      alert(`Tạo tài khoản ${form.role} thành công!`);
      setForm({ username: "", email: "", password: "", role: "MANGAKA" });
      fetchUsers();
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
    if (window.confirm(`Bạn có chắc chắn muốn ${actionText} tài khoản này?`)) {
      try {
        await updateUserStatus(id, newStatus);
        alert(`Đã ${actionText} tài khoản thành công!`);
        setUsers((prev) =>
          prev.map((u) => (u.id === id ? { ...u, status: newStatus } : u)),
        );
      } catch (error) {
        alert("Không thể thay đổi trạng thái lúc này.");
      }
    }
  };

  const handleDelete = async (id, username) => {
    if (
      window.confirm(`CẢNH BÁO: Bạn có chắc muốn XÓA tài khoản "${username}"?`)
    ) {
      try {
        await deleteMangaka(id);
        alert(`Đã xóa tài khoản "${username}".`);
        setUsers((prev) =>
          prev.map((u) => (u.id === id ? { ...u, status: "DELETED" } : u)),
        );
      } catch (error) {
        alert("Lỗi kết nối mạng hoặc hệ thống từ chối.");
      }
    }
  };
  
  return (
    <div className="tab-content" style={{ padding: "20px" }}>
      <h2 className="mb-4">👤 Quản Lý & Cấp Tài Khoản Hệ Thống</h2>
      <div className="card shadow mb-5" style={{ maxWidth: "600px" }}>
        <div className="card-header bg-primary text-white">
          Khởi Tạo Tài Khoản Mới
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
              <small className="text-muted">
                Nhập đúng địa chỉ Gmail nếu muốn tài khoản này đăng nhập được
                bằng Google
              </small>
            </div>
            <div className="mb-3">
              <label className="form-label">Loại tài khoản</label>
              <select
                name="role"
                className="form-select"
                value={form.role}
                onChange={handleChange}
              >
                <option value="MANGAKA">Tác giả</option>
                <option value="TANTOU_EDITOR">Biên tập viên</option>
              </select>
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
      <h4 className="mb-3">Danh sách Người dùng</h4>
      <div className="card shadow">
        <div className="card-body p-0">
          <table className="table table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Ngày tham gia</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-4 text-muted">
                    Chưa có tài khoản nào.
                  </td>
                </tr>
              ) : (
                users.map((u, index) => (
                  <tr key={u.userId || u.id || index}>
                    <td>#{u.userId || u.id}</td>
                    <td>
                      <strong>{u.username}</strong>
                    </td>
                    <td>{u.email}</td>
                    <td>
                      <span
                        className={`badge ${u.role === "TANTOU_EDITOR" ? "role-tantou" : "role-mangaka"}`}
                      >
                        {u.role === "TANTOU_EDITOR"
                          ? "Biên Tập Viên"
                          : "Tác Giả"}
                      </span>
                    </td>
                    <td>
                      <span
                        className={`badge ${u.status === "ACTIVE" ? "bg-success" : u.status === "DELETED" ? "bg-danger" : "bg-warning"}`}
                      >
                        {u.status}
                      </span>
                    </td>
                    <td>{formatDateOnly(u.createdAt)}</td>
                    <td>
                      <div className="d-flex gap-2 align-items-center">
                        <button
                          className={`btn btn-sm me-2 ${u.status === "ACTIVE" ? "btn-outline-warning" : "btn-outline-success"}`}
                          onClick={() =>
                            handleToggleStatus(u.id || u.userId, u.status)
                          }
                          disabled={u.status === "DELETED"}
                        >
                          {u.status === "ACTIVE" ? "Khóa" : "Mở khóa"}
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() =>
                            handleDelete(u.id || u.userId, u.username)
                          }
                          disabled={u.status === "DELETED"}
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