import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getChapterById,
  getChapterPages,
} from "../../../services/mangakaService";
import { submitChapterToEditor } from "../../../services/chapterEditorService";

export default function ChapterEditorSubmission() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState(null);
  const [pages, setPages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchData();
  }, [chapterId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [chapterData, pagesData] = await Promise.all([
        getChapterById(chapterId),
        getChapterPages(chapterId),
      ]);
      setChapter(chapterData);
      setPages(pagesData || []);
    } catch (err) {
      console.error(err);
      alert("Không thể tải thông tin chapter.");
    } finally {
      setLoading(false);
    }
  };

  const finalizedPages = pages.filter(
    (p) =>
      p.pageStatus === "DRAWING_FINALIZED" || p.status === "DRAWING_FINALIZED",
  ).length;
  const totalPages = pages.length;
  const isAllFinalized = totalPages > 0 && finalizedPages === totalPages;

  const handleSubmit = async () => {
    if (!isAllFinalized) {
      alert("Bạn phải CHỐT tất cả các trang trước khi gửi biên tập!");
      return;
    }
    if (
      window.confirm("Xác nhận gửi bản thảo này cho Biên tập viên kiểm duyệt?")
    ) {
      try {
        setSubmitting(true);
        await submitChapterToEditor(chapterId, []);
        alert("Đã gửi hồ sơ chapter cho Biên tập thành công!");
        navigate("/mangaka/manga");
      } catch (err) {
        alert(
          err?.response?.data?.message ||
            "Lỗi khi gửi chapter. Backend từ chối.",
        );
      } finally {
        setSubmitting(false);
      }
    }
  };

  if (loading)
    return (
      <div className="text-center mt-5">Đang kiểm tra tiến độ chapter...</div>
    );

  return (
    <div className="container mt-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2>Nộp bản thảo Chapter {chapter?.chapterNumber}</h2>
        <button className="btn btn-secondary" onClick={() => navigate(-1)}>
          Quay lại
        </button>
      </div>

      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body text-center p-5">
          <h4 className="mb-4 fw-bold text-primary">
            Kiểm tra điều kiện nộp bản thảo
          </h4>
          <div
            className="progress mb-4 bg-light shadow-inner position-relative"
            style={{
              height: "30px",
              fontSize: "1.05rem",
              borderRadius: "15px",
              overflow: "hidden",
            }}
          >
            <div
              className={`progress-bar ${isAllFinalized ? "bg-success" : "bg-warning progress-bar-striped progress-bar-animated"}`}
              role="progressbar"
              style={{
                width:
                  totalPages > 0
                    ? `${(finalizedPages / totalPages) * 100}%`
                    : "0%",
              }}
            ></div>
            <div
              className="position-absolute w-100 h-100 d-flex justify-content-center align-items-center fw-bold text-dark"
              style={{ top: 0, left: 0, pointerEvents: "none" }}
            >
              {finalizedPages} / {totalPages} trang đã chốt
            </div>
          </div>

          {!isAllFinalized ? (
            <div className="alert alert-danger d-inline-block shadow-sm">
              <i className="fas fa-exclamation-triangle me-2 fs-5"></i>
              Bạn chưa chốt đủ toàn bộ các trang truyện. Vui lòng quay lại không
              gian vẽ để hoàn tất!
            </div>
          ) : (
            <div className="alert alert-success d-inline-block fw-bold shadow-sm">
              <i className="fas fa-check-circle me-2 fs-5"></i>
              Tuyệt vời! Tất cả các trang đã được chốt. Bản thảo đã sẵn sàng để
              gửi.
            </div>
          )}

          <div className="mt-5 border-top pt-4">
            <button
              className="btn btn-primary btn-lg fw-bold px-5 shadow"
              onClick={handleSubmit}
              disabled={!isAllFinalized || submitting}
            >
              {submitting ? "Đang xử lý..." : "🚀 Chốt Và Nộp Cho Biên Tập"}
            </button>
            <p className="text-muted mt-3 small w-75 mx-auto lh-base">
              Hệ thống sẽ tự động tổng hợp các trang đã chốt của bạn gửi thẳng
              đến Biên tập viên phụ trách.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
