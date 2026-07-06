import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getChapterPages, getChapterById } from "../../services/mangakaService";
import { resolveImageUrl } from "../../utils/imageUrl";
export default function ChapterPages() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [chapter, setChapter] = useState(null);
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [chapterData, pagesData] = await Promise.all([
          getChapterById(chapterId),
          getChapterPages(chapterId),
        ]);
        setChapter(chapterData);
        setPages(pagesData || []);
      } catch (error) {
        console.error("Lỗi khi tải dữ liệu chapter:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [chapterId]);
  if (loading)
    return <div className="text-center mt-5">Đang tải danh sách trang...</div>;
  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>
          Quản lý các trang - Chapter {chapter?.chapterNumber ?? "?"}
          {chapter?.title ? `: ${chapter.title}` : ""}
        </h2>
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
                  src={resolveImageUrl(
                    page.imageUrl,
                    "https://placehold.co/200x300?text=Trang+" +
                      page.pageNumber,
                  )}
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
                      navigate(`/mangaka/pages/${page.id}/drawing`, {
                        state: { originalImageUrl: page.imageUrl },
                      })
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