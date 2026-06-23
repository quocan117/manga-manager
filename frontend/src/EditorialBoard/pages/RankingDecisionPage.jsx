import React, { useState, useEffect } from "react";
// Đã tháo bỏ: import { rankingData } from "../../data/mockData";

const RankingDecisionPage = () => {
  const [seriesList, setSeriesList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  // 1. GỌI API VÀ SẮP XẾP TỪ DƯỚI LÊN
  useEffect(() => {
    const fetchRankingForBoard = async () => {
      try {
        const response = await fetch("http://localhost:8080/manga-series");
        if (response.ok) {
          const data = await response.json();

          // Tính tổng Like và gán vào mỗi series
          const processedData = data.map((series) => {
            const totalLikes =
              series.chapters?.reduce((sum, ch) => sum + ch.likes, 0) || 0;
            return { ...series, totalLikes };
          });

          // SẮP XẾP TĂNG DẦN: Truyện ít Like nhất (bét bảng) sẽ trồi lên đầu tiên
          processedData.sort((a, b) => a.totalLikes - b.totalLikes);

          setSeriesList(processedData);
        } else {
          console.error("Lỗi khi tải dữ liệu xếp hạng.");
        }
      } catch (error) {
        console.error("Lỗi kết nối API:", error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchRankingForBoard();
  }, []);

  // 2. XỬ LÝ HÀNH ĐỘNG "TRẢM TRUYỆN"
  const handleCancelSeries = async (id, title) => {
    if (
      window.confirm(
        `CẢNH BÁO: Bạn có chắc chắn muốn ĐÌNH CHỈ bộ truyện "${title}"? Hành động này sẽ ẩn truyện khỏi màn hình độc giả.`,
      )
    ) {
      /*
      // ĐOẠN CODE GỌI API THẬT (Nhờ team Backend cung cấp URL chuẩn)
      try {
        // Ví dụ URL: /editorial-board/series/{id}/cancel
        const response = await fetch(`http://localhost:8080/editorial-board/series/${id}/cancel`, {
          method: "PUT", 
        });
        
        if (!response.ok) {
          alert("Lỗi máy chủ! Không thể hủy bộ truyện lúc này.");
          return; 
        }
      } catch (error) {
        console.error("Lỗi gọi API Hủy truyện:", error);
        return;
      }
      */

      // Cập nhật lại giao diện ngay lập tức nếu hủy thành công
      setSeriesList((prevList) =>
        prevList.map((s) => (s.id === id ? { ...s, status: "CANCELLED" } : s)),
      );
      alert(`Đã đình chỉ thành công bộ truyện "${title}".`);
    }
  };

  if (isLoading)
    return (
      <div className="tab-content">
        <h2>Đang tải dữ liệu...</h2>
      </div>
    );

  // Lọc ra danh sách truyện đang hoạt động (không tính những bộ đã bị hủy trước đó)
  // để Hội đồng dễ nhìn. Nếu bạn muốn hiển thị cả bộ đã hủy thì bỏ .filter() này đi.
  const activeSeriesList = seriesList.filter((s) => s.status !== "CANCELLED");

  return (
    <div className="tab-content">
      <h2>Bảng Xếp Hạng & Phán Quyết Sinh Tử</h2>
      <p>
        Theo dõi thứ hạng (từ thấp lên cao) để ra quyết định duy trì hoặc hủy bỏ
        series.
      </p>

      <table className="admin-table">
        <thead>
          <tr>
            <th>Hạng (Từ dưới lên)</th>
            <th>Tên Series</th>
            <th>Tác giả</th>
            <th>Tổng Lượt Thích</th>
            <th>Trạng Thái</th>
            <th>Phán Quyết</th>
          </tr>
        </thead>
        <tbody>
          {activeSeriesList.length > 0 ? (
            activeSeriesList.map((series, index) => {
              // Logic cảnh báo: 3 bộ truyện có ít Like nhất sẽ bị highlight đỏ
              const isDanger = index < 3;

              return (
                <tr key={series.id} className={isDanger ? "row-danger" : ""}>
                  {/* Do đã sort tăng dần, index 0 là bét bảng, nên hạng = Tổng số truyện - index */}
                  <td>#{activeSeriesList.length - index}</td>
                  <td>
                    <strong>{series.title}</strong>
                  </td>
                  <td>{series.author || "Chưa cập nhật"}</td>
                  <td>{series.totalLikes.toLocaleString()}</td>
                  <td>
                    {isDanger ? (
                      <span className="badge badge-danger">Nguy cơ hủy</span>
                    ) : (
                      <span className="badge badge-success">An toàn</span>
                    )}
                  </td>
                  <td>
                    {isDanger ? (
                      <button
                        className="btn-cancel-series"
                        onClick={() =>
                          handleCancelSeries(series.id, series.title)
                        }
                      >
                        Đình chỉ Series
                      </button>
                    ) : (
                      <span
                        style={{
                          color: "#7f8c8d",
                          fontStyle: "italic",
                          fontSize: "0.9rem",
                        }}
                      >
                        Tiếp tục duy trì
                      </span>
                    )}
                  </td>
                </tr>
              );
            })
          ) : (
            <tr>
              <td colSpan="6" style={{ textAlign: "center", padding: "20px" }}>
                Không có dữ liệu hiển thị.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default RankingDecisionPage;
