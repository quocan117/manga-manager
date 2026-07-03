import React, { useState, useEffect } from "react";
import { getAssistants, createAssistant } from "../../services/mangakaService";
import "../../EditorialBoard/styles/EditorialBoard.css"; 

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

      alert(`Tạo tài khoản trợ lý thành công!`);
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

  return (
    <div className="tab-content" style={{ padding: "20px" }}>
      <h2 className="mb-4">👤 Quản Lý Tài Khoản Trợ Lý</h2>

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
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
              </tr>
            </thead>
            <tbody>
              {assistants.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center py-4 text-muted">
                    Bạn chưa có trợ lý nào.
                  </td>
                </tr>
              ) : (
                assistants.map((ast) => (
                  <tr key={ast.id}>
                    <td>#{ast.id}</td>
                    <td>
                      <img
                        src={
                          ast.avatarUrl ||
                          "https://placehold.co/40x40?text=No+Avatar"
                        }
                        alt="avatar"
                        style={{
                          width: "40px",
                          height: "40px",
                          borderRadius: "50%",
                        }}
                      />
                    </td>
                    <td>
                      <strong>{ast.username}</strong>
                    </td>
                    <td>{ast.email}</td>
                    <td>
                      <span className="badge bg-info text-dark">Trợ lý</span>
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
