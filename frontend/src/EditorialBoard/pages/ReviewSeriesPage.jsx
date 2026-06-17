import React from "react";
import { pendingSeriesData } from "../../data/mockData";

const ApproveSeriesPage = () => {
  return (
    <div className="tab-content">
      <h2>Xét Duyệt Series Mới</h2>
      <p>Bỏ phiếu thông qua series mới và quyết định lịch xuất bản.</p>

      <div className="card-list">
        {pendingSeriesData.map((series) => (
          <div key={series.id} className="approval-card">
            <div className="card-info">
              <h3>{series.title}</h3>
              <p>
                <strong>Tác giả:</strong> {series.author} |{" "}
                <strong>Thể loại:</strong> {series.genre}
              </p>
              <p>
                <strong>Ngày nộp:</strong> {series.submissionDate}
              </p>
              <p className="card-desc">"{series.description}"</p>
            </div>
            <div className="card-actions">
              <select className="schedule-select" defaultValue="">
                <option value="" disabled>
                  -- Chọn lịch --
                </option>
                <option value="weekly">Hàng Tuần</option>
                <option value="monthly">Hàng Tháng</option>
              </select>
              <button
                className="btn-approve"
                onClick={() => alert("Đã phê duyệt!")}
              >
                Duyệt & Xuất bản
              </button>
              <button
                className="btn-reject"
                onClick={() => alert("Đã từ chối!")}
              >
                Từ chối
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ApproveSeriesPage;
