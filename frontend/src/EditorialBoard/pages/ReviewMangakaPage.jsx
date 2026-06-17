import React from "react";
import { pendingMangakaData } from "../../data/mockData";

const ApproveMangakaPage = () => {
  return (
    <div className="tab-content">
      <h2>Xét Duyệt Ứng Viên Mangaka</h2>
      <table className="admin-table">
        <thead>
          <tr>
            <th>Tên Ứng Viên</th>
            <th>Email</th>
            <th>Ngày nộp hồ sơ</th>
            <th>Portfolio</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {pendingMangakaData.map((user) => (
            <tr key={user.id}>
              <td>{user.name}</td>
              <td>{user.email}</td>
              <td>{user.applyDate}</td>
              <td>
                <a href={user.portfolioUrl} target="_blank" rel="noreferrer">
                  Xem Portfolio
                </a>
              </td>
              <td>
                <button
                  className="btn-approve-sm"
                  style={{ marginRight: "5px" }}
                >
                  Cấp tài khoản
                </button>
                <button className="btn-reject-sm">Từ chối</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ApproveMangakaPage;
