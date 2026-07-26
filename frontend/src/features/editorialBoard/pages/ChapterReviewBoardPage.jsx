import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getChapterForBoardReview,
  reviewChapter,
} from "../../../services/boardService";
import SeriesFileList from "../../../components/SeriesFileList";

export default function ChapterReviewBoardPage() {
  const { chapterId } = useParams();
  const navigate = useNavigate();
  const [chapter, setChapter] = useState(null);
  const [comment, setComment] = useState("");

  useEffect(() => {
    getChapterForBoardReview(chapterId).then(setChapter);
  }, [chapterId]);

  const handleReview = async (confirmed) => {
    if (!confirmed && !comment) {
      alert("Vui lòng nhập nhận xét khi trả về Tantou Editor.");
      return;
    }
    try {
      await reviewChapter(chapterId, confirmed, comment);
      alert(
        confirmed
          ? "Đã xác nhận chapter đủ điều kiện."
          : "Đã trả về Tantou Editor.",
      );
      navigate("/board/series-management");
    } catch (error) {
      alert("Lỗi khi gửi xác nhận.");
    }
  };

  if (!chapter) return <div className="p-4">Đang tải chapter...</div>;

  return (
    <div className="tab-content">
      <h2>
        Xác nhận Chapter #{chapter.chapterNumber}: {chapter.title}
      </h2>
      <p className="text-muted">Series: {chapter.seriesTitle}</p>
      <div className="mb-3">
        <SeriesFileList
          files={chapter.manuscriptFiles || []}
          emptyText="Chưa có file bản thảo nào."
        />
      </div>
      <textarea
        className="form-control mb-3"
        rows={4}
        placeholder="Nhận xét (bắt buộc nếu trả về)"
        value={comment}
        onChange={(e) => setComment(e.target.value)}
      />
      <div className="d-flex gap-2">
        <button className="btn btn-success" onClick={() => handleReview(true)}>
          ✅ Đủ điều kiện xuất bản
        </button>
        <button
          className="btn btn-outline-danger"
          onClick={() => handleReview(false)}
        >
          ❌ Trả về chỉnh sửa
        </button>
      </div>
    </div>
  );
}
