import React from "react";
import { rankingData } from "../../data/mockData";

const RankingDecisionPage = () => {
  return (
    <div className="tab-content">
      <h2>Bảng Xếp Hạng & Phán Quyết Sinh Tử</h2>
      <p>Theo dõi thứ hạng để ra quyết định duy trì hoặc hủy bỏ series.</p>

      <table className="admin-table">
        <thead>
          <tr>
            <th>Hạng</th>
            <th>Tên Series</th>
            <th>Tác giả</th>
            <th>Tổng Lượt Thích</th>
            <th>Trạng Thái</th>
            <th>Phán Quyết</th>
          </tr>
        </thead>
        <tbody>
          {rankingData.map((series) => (
            <tr
              key={series.id}
              className={series.trend === "danger" ? "row-danger" : ""}
            >
              <td>#{series.rank}</td>
              <td>
                <strong>{series.title}</strong>
              </td>
              <td>{series.author}</td>
              <td>{series.totalLikes.toLocaleString()}</td>
              <td>
                {series.trend === "danger" ? (
                  <span className="badge badge-danger">Nguy cơ hủy</span>
                ) : (
                  <span className="badge badge-success">An toàn</span>
                )}
              </td>
              <td>
                {series.trend === "danger" ? (
                  <button
                    className="btn-cancel-series"
                    onClick={() =>
                      alert(`Đã ra lệnh HỦY series: ${series.title}`)
                    }
                  >
                    Hủy Series
                  </button>
                ) : (
                  <span
                    style={{
                      color: "#7f8c8d",
                      fontStyle: "italic",
                      fontSize: "0.9rem",
                    }}
                  >
                    Giao Biên tập viên duy trì
                  </span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default RankingDecisionPage;
