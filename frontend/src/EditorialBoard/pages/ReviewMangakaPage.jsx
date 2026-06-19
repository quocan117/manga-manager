import React, { useState, useEffect } from "react";
import { pendingMangakaData } from "../../data/mockData";

const ReviewMangakaPage = () => {
  const [candidates, setCandidates] = useState([]);
  const [activeTab, setActiveTab] = useState("Pending");
  const [searchQuery, setSearchQuery] = useState("");
  const [rejectModal, setRejectModal] = useState({
    isOpen: false,
    candidate: null,
    reason: "",
  });

  // 1. LẤY DANH SÁCH ỨNG VIÊN
  useEffect(() => {
    /*
    const fetchCandidates = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/admin/mangaka-applications");
        if (response.ok) {
          const data = await response.json();
          setCandidates(data);
        }
      } catch (error) {
        console.error("Lỗi khi tải danh sách:", error);
      }
    };
    fetchCandidates();
    */
    setCandidates(pendingMangakaData);
  }, []);

  // 2. DUYỆT ỨNG VIÊN
  const handleApprove = async (id) => {
    if (window.confirm("Bạn muốn cấp tài khoản Mangaka cho người này?")) {
      /*
      try {
        const response = await fetch(`http://localhost:8080/api/admin/mangaka-applications/${id}/approve`, {
          method: "POST",
        });
        if (!response.ok) {
          alert("Lỗi server, không thể duyệt!");
          return; // Dừng lại nếu API lỗi
        }
      } catch (error) {
        console.error("Lỗi gọi API:", error);
        return;
      }
      */

      // Cập nhật giao diện 
      setCandidates(
        candidates.map((c) => (c.id === id ? { ...c, status: "Approved" } : c)),
      );
    }
  };

  // 3. TỪ CHỐI ỨNG VIÊN
  const openRejectModal = (candidate) => {
    setRejectModal({ isOpen: true, candidate: candidate, reason: "" });
  };

  const submitReject = async () => {
    if (!rejectModal.reason.trim()) {
      alert("Vui lòng nhập lý do từ chối!");
      return;
    }
    /*
    try {
      const response = await fetch(`http://localhost:8080/api/admin/mangaka-applications/${rejectModal.candidate.id}/reject`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason: rejectModal.reason }),
      });
      if (!response.ok) {
        alert("Lỗi server, không thể từ chối!");
        return;
      }
    } catch (error) {
      console.error("Lỗi gọi API:", error);
      return;
    }
    */

    //  Cập nhật giao diện 
    setCandidates(
      candidates.map((c) =>
        c.id === rejectModal.candidate.id
          ? { ...c, status: "Rejected", rejectReason: rejectModal.reason }
          : c,
      ),
    );
    setRejectModal({ isOpen: false, candidate: null, reason: "" });
  };

  // --- LỌC VÀ HIỂN THỊ DỮ LIỆU ---
  const filteredData = candidates.filter((c) => {
    const isMatchTab = c.status === activeTab;
    const isMatchSearch =
      c.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      c.email?.toLowerCase().includes(searchQuery.toLowerCase());
    return isMatchTab && isMatchSearch;
  });

  return (
    <div className="tab-content">
      <div className="board-header">
        <h2>Xét Duyệt Ứng Viên Mangaka</h2>
        <input
          type="text"
          placeholder="🔍 Tìm tên, email..."
          className="search-input-board"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>

      <div className="board-tabs">
        {["Pending", "Approved", "Rejected"].map((tab) => (
          <button
            key={tab}
            className={`tab-btn ${activeTab === tab ? "active" : ""}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab === "Pending"
              ? "Đang chờ duyệt"
              : tab === "Approved"
                ? "Đã chấp nhận"
                : "Đã từ chối"}
          </button>
        ))}
      </div>

      <table className="admin-table">
        <thead>
          <tr>
            <th>Tên Ứng Viên</th>
            <th>Email</th>
            <th>Ngày nộp</th>
            <th>Portfolio</th>
            {activeTab === "Pending" && <th>Hành động</th>}
            {activeTab === "Rejected" && <th>Lý do từ chối</th>}
          </tr>
        </thead>
        <tbody>
          {filteredData.length > 0 ? (
            filteredData.map((user) => (
              <tr key={user.id}>
                <td>
                  <strong>{user.name}</strong>
                </td>
                <td>{user.email}</td>
                <td>{user.applyDate}</td>
                <td>
                  <a
                    href={user.portfolioUrl}
                    target="_blank"
                    rel="noreferrer"
                    className="portfolio-link"
                  >
                    Xem Portfolio
                  </a>
                </td>

                {activeTab === "Pending" && (
                  <td>
                    <button
                      className="btn-approve-sm action-btn"
                      onClick={() => handleApprove(user.id)}
                    >
                      Duyệt
                    </button>
                    <button
                      className="btn-reject-sm action-btn"
                      onClick={() => openRejectModal(user)}
                    >
                      Từ chối
                    </button>
                  </td>
                )}

                {activeTab === "Rejected" && (
                  <td className="reject-reason-text">
                    {user.rejectReason || "Không có lý do"}
                  </td>
                )}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="6" className="empty-row">
                Không có hồ sơ nào.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      {rejectModal.isOpen && (
        <div className="mangaka-modal-overlay">
          <div className="mangaka-modal-box">
            <h3 className="text-danger" style={{ marginTop: 0 }}>
              Từ chối: {rejectModal.candidate?.name}
            </h3>
            <p style={{ fontSize: "0.95rem", color: "#555" }}>
              Nhập lý do từ chối để hệ thống gửi email phản hồi cho ứng viên.
            </p>
            <textarea
              rows="4"
              placeholder="Ví dụ: Portfolio chưa đủ kinh nghiệm..."
              value={rejectModal.reason}
              onChange={(e) =>
                setRejectModal({ ...rejectModal, reason: e.target.value })
              }
            />
            <div className="mangaka-modal-actions">
              <button
                className="btn-cancel"
                onClick={() =>
                  setRejectModal({ isOpen: false, candidate: null, reason: "" })
                }
              >
                Hủy
              </button>
              <button className="btn-reject" onClick={submitReject}>
                Xác nhận từ chối
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReviewMangakaPage;