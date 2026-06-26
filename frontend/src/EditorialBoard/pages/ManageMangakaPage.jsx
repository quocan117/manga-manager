import React, { useState, useEffect } from "react";
import {
  getUsers,
  createMangaka,
  updateUserStatus,
  deleteMangaka,
} from "../../services/boardService";
import "../styles/EditorialBoard.css";

export default function ManageMangakaPage() {
  const [mangakas, setMangakas] = useState([]);
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });

  useEffect(() => {
    fetchMangakas();
  }, []);

  const fetchMangakas = async () => {
    try {
      const data = await getUsers();
      const filteredMangakas = data.filter((user) => user.role === "MANGAKA");
      setMangakas(filteredMangakas);
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

    const payload = {
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
      role: "MANGAKA",
      status: "ACTIVE",
    };

    try {
      setLoading(true);
      await createMangaka(payload);

      alert("Tạo tài khoản Mangaka thành công!");
      setForm({ username: "", email: "", password: "" });
      fetchMangakas();
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
        setMangakas((prev) =>
          prev.map((m) => (m.id === id ? { ...m, status: newStatus } : m)),
        );
      } catch (error) {
        console.error("Lỗi cập nhật trạng thái:", error);
        alert("Không thể thay đổi trạng thái lúc này.");
      }
    }
  };

  const handleDelete = async (id, username) => {
    if (
      window.confirm(
        `CẢNH BÁO: Bạn có chắc chắn muốn XÓA tài khoản tác giả "${username}"? Hành động này sẽ chuyển trạng thái thành DELETED.`,
      )
    ) {
      try {
        await deleteMangaka(id);
        alert(`Đã xóa tài khoản "${username}".`);

        setMangakas((prev) =>
          prev.map((m) =>
            m.id === id ? { ...m, status: "DELETED" } : m,
          ),
        );
      } catch (error) {
        console.error("Lỗi xóa tài khoản:", error);
        if (error.response) {
          alert(
            `Lỗi từ hệ thống: ${error.response.data.message || "Xóa thất bại"}`,
          );
        } else {
          alert("Lỗi kết nối mạng hoặc Server đang tắt.");
        }
      }
    }
  };

  return (
    <div className="tab-content" style={{ padding: "20px" }}>
      <h2 className="mb-4">👤 Quản Lý & Cấp Tài Khoản Mangaka</h2>

      <div className="card shadow mb-5" style={{ maxWidth: "600px" }}>
        <div className="card-header bg-primary text-white">
          Khởi Tạo Tài Khoản Tác Giả
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
                placeholder="Nhập username..."
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
                placeholder="example@studio.com"
                required
              />
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

      <h4 className="mb-3">Danh sách Mangaka hiện tại</h4>
      <div className="card shadow">
        <div className="card-body p-0">
          <table className="table table-hover mb-0">
            <thead className="table-light">
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Trạng thái</th>
                <th>Ngày tham gia</th>
                <th>Người cấp</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {mangakas.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center py-4 text-muted">
                    Chưa có tài khoản Mangaka nào trên hệ thống.
                  </td>
                </tr>
              ) : (
                mangakas.map((m, index) => (
                  <tr key={m.userId || m.id || index}>
                    <td>#{m.userId || m.id}</td>
                    <td>
                      <strong>{m.username}</strong>
                    </td>
                    <td>{m.email}</td>
                    <td>
                      <span
                        className={`badge ${
                          m.status === "ACTIVE"
                            ? "bg-success"
                            : m.status === "DELETED"
                              ? "bg-danger"
                              : "bg-warning"
                        }`}
                      >
                        {m.status}
                      </span>
                    </td>
                    <td>{new Date(m.createdAt).toLocaleDateString()}</td>
                    <td>{m.createdByUsername || "Hệ thống"}</td>

                    <td>
                      <div className="d-flex gap-2 align-items-center">
                        <button
                          className={`btn btn-sm me-2 ${m.status === "ACTIVE" ? "btn-outline-warning" : "btn-outline-success"}`}
                          onClick={() =>
                            handleToggleStatus(m.id || m.userId, m.status)
                          }
                          disabled={m.status === "DELETED"}
                        >
                          {m.status === "ACTIVE" ? "Khóa" : "Mở khóa"}
                        </button>

                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() =>
                            handleDelete(m.id || m.userId, m.username)
                          }
                          disabled={m.status === "DELETED"}
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
