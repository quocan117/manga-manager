import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getChapterPages } from "../../services/mangakaService";

export default function ChapterPages() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPages = async () => {
      try {
        const data = await getChapterPages(chapterId);
        setPages(data || []);
      } catch (error) {
        console.error("Lỗi khi tải trang:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchPages();
  }, [chapterId]);

  if (loading)
    return <div className="text-center mt-5">Đang tải danh sách trang...</div>;

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Quản lý các trang - Chapter {chapterId}</h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Quay lại
        </button>
      </div>

      <div className="row">
        {pages.length === 0 ? (
          <div className="col-12 text-center text-muted">
            Chapter này chưa có trang nào.
          </div>
        ) : (
          pages.map((page) => (
            <div className="col-md-3 mb-4" key={page.id}>
              <div className="card shadow-sm">
                <img
                  src={
                    page.imageUrl
                      ? `http://localhost:8080/covers/${page.imageUrl}`
                      : "https://placehold.co/200x300?text=Trang+" +
                        page.pageNumber
                  }
                  className="card-img-top"
                  alt={`Page ${page.pageNumber}`}
                  style={{ height: "300px", objectFit: "cover" }}
                />
                <div className="card-body text-center">
                  <h5 className="card-title">Trang {page.pageNumber}</h5>
                  <span className="badge bg-secondary mb-3">{page.status}</span>
                  <br />
                  <button
                    className="btn btn-primary w-100"
                    onClick={() =>
                      navigate(`/mangaka/pages/${page.id}/drawing`)
                    }
                  >
                    🖌️ Chỉnh sửa / Đánh dấu
                  </button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
