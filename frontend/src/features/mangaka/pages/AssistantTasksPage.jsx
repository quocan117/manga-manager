import { useEffect, useState } from "react";
import {
  getAssistants,
  assignTask,
  getChapterTasks,
  getMySeries,
  getSeriesChapters,
  getChapterPages,
  reviewSubmission,
  getChapterSubmissions,
} from "../../../services/mangakaService";
import { formatDateTime } from "../../../utils/formatDate";
import SeriesFileList from "../../../components/SeriesFileList";
import { fetchSeriesFileBlob } from "../../../services/seriesFileService";

export default function AssistantTasks() {
  const [assistants, setAssistants] = useState([]);
  const [seriesList, setSeriesList] = useState([]);
  const [chapterList, setChapterList] = useState([]);
  const [pageList, setPageList] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [selectedSeriesId, setSelectedSeriesId] = useState("");
  const [selectedChapterId, setSelectedChapterId] = useState("");
  const [submissions, setSubmissions] = useState([]);

  const [form, setForm] = useState({
    pageId: "",
    assistantId: "",
    taskType: "BACKGROUND",
    title: "",
    description: "",
    dueDate: "",
  });

  const [originalFiles, setOriginalFiles] = useState([]);
  const [fileError, setFileError] = useState("");

  useEffect(() => {
    fetchAssistants();
    fetchSeries();
  }, []);

  const fetchAssistants = async () => {
    try {
      const data = await getAssistants();
      setAssistants(data);
    } catch (error) {
      console.error(error);
    }
  };

  const fetchSeries = async () => {
    try {
      const data = await getMySeries();
      setSeriesList(data || []);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    if (selectedSeriesId) {
      fetchChapters(selectedSeriesId);
    } else {
      setChapterList([]);
      setSelectedChapterId("");
    }
  }, [selectedSeriesId]);

  const fetchChapters = async (seriesId) => {
    try {
      const data = await getSeriesChapters(seriesId);
      setChapterList(data || []);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    if (selectedChapterId) {
      fetchTasks();
      fetchPages();
      fetchSubmissions();
    } else {
      setTasks([]);
      setPageList([]);
      setSubmissions([]);
      setForm((prev) => ({ ...prev, pageId: "" }));
    }
  }, [selectedChapterId]);

  const fetchTasks = async () => {
    if (!selectedChapterId) return;
    try {
      const data = await getChapterTasks(selectedChapterId);
      setTasks(data || []);
    } catch (error) {
      console.error(error);
    }
  };

  const fetchPages = async () => {
    if (!selectedChapterId) return;
    try {
      const data = await getChapterPages(selectedChapterId);
      setPageList(data || []);
    } catch (error) {
      console.error(error);
    }
  };

  const fetchSubmissions = async () => {
    if (!selectedChapterId) return;
    try {
      const data = await getChapterSubmissions(selectedChapterId);
      setSubmissions(data || []);
    } catch (error) {
      console.error(error);
    }
  };

  const handleReview = async (submission, decision) => {
    let reviewNote = null;
    if (decision === "REVISION_REQUESTED") {
      reviewNote = window.prompt(`Nhập lý do yêu cầu trợ lý sửa lại bài:`, "");
      if (reviewNote === null) return;
      if (!reviewNote.trim()) {
        alert("Vui lòng nhập lý do khi yêu cầu sửa lại");
        return;
      }
    }
    try {
      await reviewSubmission(submission.id, decision, reviewNote);
      alert(
        decision === "APPROVED"
          ? "Đã duyệt bài nộp!"
          : "Đã yêu cầu trợ lý sửa lại.",
      );
      fetchSubmissions();
      fetchTasks();
    } catch (error) {
      alert("Duyệt bài thất bại: " + (error.response?.data?.message || ""));
    }
  };

  const findTaskTitle = (taskId) =>
    tasks.find((t) => (t.id || t.taskId) === taskId)?.title ||
    `Task #${taskId}`;
  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleFilePick = (e) => {
    const picked = Array.from(e.target.files || []);
    if (picked.length === 0) return;

    setOriginalFiles((prev) => {
      const combined = [...prev, ...picked];

      const zipCount = combined.filter((f) =>
        f.name.toLowerCase().endsWith(".zip"),
      ).length;
      if (zipCount > 1) {
        setFileError(
          "Chỉ được chọn tối đa 1 file .zip trong một lần giao việc.",
        );
        return prev;
      }
      if (combined.length > 20) {
        setFileError("Chỉ được chọn tối đa 20 file.");
        return prev;
      }
      setFileError("");
      return combined;
    });
    e.target.value = "";
  };

  const removeOriginalFile = (index) => {
    setOriginalFiles((prev) => prev.filter((_, i) => i !== index));
    setFileError("");
  };

  const handleAssign = async (e) => {
    e.preventDefault();
    if (originalFiles.length === 0) {
      alert(
        "Vui lòng chọn ít nhất 1 ảnh gốc hoặc 1 file .zip để giao cho trợ lý.",
      );
      return;
    }
    const formData = new FormData();
    formData.append("pageId", Number(form.pageId));
    formData.append("assistantId", Number(form.assistantId));
    formData.append("taskType", form.taskType);
    formData.append("title", form.title);
    if (form.description) formData.append("description", form.description);
    const formattedDueDate =
      form.dueDate.length === 16 ? `${form.dueDate}:00` : form.dueDate;
    formData.append("dueDate", formattedDueDate);
    formData.append("areaX", 0);
    formData.append("areaY", 0);
    formData.append("areaWidth", 100);
    formData.append("areaHeight", 100);
    originalFiles.forEach((file) => formData.append("originalFiles", file));
    try {
      await assignTask(formData);
      alert("Giao việc cho trợ lý thành công!");
      setForm({ ...form, title: "", description: "", pageId: "" });
      setOriginalFiles([]);
      fetchTasks();
    } catch (error) {
      console.error(error);
      alert(
        "Giao việc thất bại: " +
          (error.response?.data?.message || "Vui lòng kiểm tra lại dữ liệu."),
      );
    }
  };

  const handleDownloadFile = async (file) => {
    try {
      const blob = await fetchSeriesFileBlob(file.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = file.originalFileName || "file_ho_thanh";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      alert("Không thể tải xuống tệp: " + (err.message || "Lỗi mạng"));
    }
  };

  const selectedChapterData = chapterList.find(
    (c) => c.id === Number(selectedChapterId),
  );

  return (
    <div>
      <h2 className="mb-4">Giao việc trợ lý</h2>
      <div className="row">
        <div className="col-md-5">
          <div className="card shadow mb-4">
            <div className="card-header bg-white fw-bold">Giao việc</div>
            <div className="card-body">
              <form onSubmit={handleAssign}>
                <div className="mb-3">
                  <label className="form-label text-primary fw-bold">
                    1. Chọn Series
                  </label>
                  <select
                    className="form-select border-primary"
                    value={selectedSeriesId}
                    onChange={(e) => setSelectedSeriesId(e.target.value)}
                  >
                    <option value="">-- Chọn Series --</option>
                    {seriesList.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.title}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label text-primary fw-bold">
                    2. Chọn Chapter
                  </label>
                  <select
                    className="form-select border-primary"
                    value={selectedChapterId}
                    onChange={(e) => setSelectedChapterId(e.target.value)}
                    disabled={!selectedSeriesId}
                  >
                    <option value="">-- Chọn Chapter --</option>
                    {chapterList.map((c) => (
                      <option key={c.id} value={c.id}>
                        Chapter {c.chapterNumber}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label text-primary fw-bold">
                    3. Chọn Trang Truyện
                  </label>
                  <select
                    name="pageId"
                    className="form-select border-primary"
                    value={form.pageId}
                    onChange={handleChange}
                    required
                    disabled={pageList.length === 0}
                  >
                    <option value="">
                      {pageList.length === 0
                        ? "Vui lòng chọn Chapter trước"
                        : "--- Chọn Trang để giao việc ---"}
                    </option>
                    {pageList.map((page) => (
                      <option key={page.id} value={page.id}>
                        Trang {page.pageNumber}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="mb-3">
                  <label className="form-label">
                    File tài liệu gốc (ảnh gốc, có thể thêm 1 file .zip)
                  </label>
                  <input
                    type="file"
                    className="form-control"
                    multiple
                    accept="image/*,.zip,.pdf,.doc,.docx,.txt,.md"
                    onChange={handleFilePick}
                  />
                  <small className="text-muted d-block mt-1">
                    Có thể chọn nhiều lần để gộp thêm ảnh/zip vào danh sách bên
                    dưới (tối đa 20 file, mỗi ảnh ≤20MB, zip ≤100MB, tổng
                    ≤200MB, chỉ 1 file .zip).
                  </small>
                  {fileError && (
                    <div className="text-danger small mt-1">{fileError}</div>
                  )}
                  {originalFiles.length > 0 && (
                    <ul className="submit-series-filelist mt-2">
                      {originalFiles.map((f, i) => (
                        <li key={`${f.name}-${i}`}>
                          <span>{f.name}</span>
                          <span className="text-muted small">
                            ({Math.round(f.size / 1024)} KB)
                          </span>
                          <button
                            type="button"
                            className="btn-close btn-sm"
                            aria-label="Xoá"
                            onClick={() => removeOriginalFile(i)}
                          />
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <hr className="my-4" />
                <div className="mb-3">
                  <label className="form-label">Chọn Trợ lý </label>
                  <select
                    name="assistantId"
                    className="form-select"
                    value={form.assistantId}
                    onChange={handleChange}
                    required
                  >
                    <option value="">-- Select Assistant --</option>
                    {assistants.map((assistant) => (
                      <option key={assistant.id} value={assistant.id}>
                        {assistant.username}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Loại Công Việc</label>
                  <select
                    name="taskType"
                    className="form-select"
                    value={form.taskType}
                    onChange={handleChange}
                  >
                    <option value="BACKGROUND">BACKGROUND</option>
                    <option value="TEXT">TEXT</option>
                    <option value="EFFECTS">EFFECTS</option>
                    <option value="OTHER">OTHER</option>
                  </select>
                </div>
                <div className="mb-3">
                  <label className="form-label">Tiêu đề</label>
                  <input
                    type="text"
                    name="title"
                    className="form-control"
                    value={form.title}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="mb-3">
                  <label className="form-label">Mô tả chi tiết</label>
                  <textarea
                    name="description"
                    className="form-control"
                    rows="3"
                    value={form.description}
                    onChange={handleChange}
                  />
                </div>
                <div className="mb-4">
                  <label className="form-label">Hạn chót</label>
                  <input
                    type="datetime-local"
                    name="dueDate"
                    className="form-control"
                    value={form.dueDate}
                    onChange={handleChange}
                    required
                  />
                </div>
                <button type="submit" className="btn btn-success w-100 fw-bold">
                  Giao việc
                </button>
              </form>
            </div>
          </div>
        </div>
        <div className="col-md-7">
          <div className="card shadow">
            <div className="card-header bg-white py-3">
              <span className="fw-bold fs-5">
                Việc hiện tại{" "}
                {selectedChapterData &&
                  `- Chapter ${selectedChapterData.chapterNumber}`}
              </span>
            </div>
            <div>
              <div className="card-header bg-white py-3">
                <span className="fw-bold fs-5">📥 Bài nộp từ Trợ lý</span>
              </div>
              <div className="card-body">
                {submissions.length === 0 ? (
                  <p className="text-muted text-center mb-0">
                    {selectedChapterId
                      ? "Chưa có bài nộp nào cho chapter này."
                      : "Vui lòng chọn Chapter."}
                  </p>
                ) : (
                  submissions.map((s) => (
                    <div key={s.id} className="border rounded p-3 mb-3">
                      <div className="d-flex justify-content-between align-items-start">
                        <div>
                          <strong>{findTaskTitle(s.taskId)}</strong>
                          <div className="small text-muted">
                            Trợ lý: {s.submittedByName} · Nộp lúc:{" "}
                            {s.submittedAt
                              ? formatDateTime(s.submittedAt)
                              : "-"}
                          </div>
                        </div>
                        <span
                          className={`badge ${
                            s.status === "APPROVED"
                              ? "bg-success"
                              : s.status === "REVISION_REQUESTED"
                                ? "bg-danger"
                                : "bg-warning text-dark"
                          }`}
                        >
                          {s.status}
                        </span>
                      </div>

                      {s.note && (
                        <p className="small mt-2 mb-1">
                          Ghi chú trợ lý: {s.note}
                        </p>
                      )}

                      <div className="mt-2">
                        <SeriesFileList
                          files={s.resultFiles}
                          emptyText="Trợ lý chưa gửi kèm file nào."
                        />
                        {s.resultFiles && s.resultFiles.length > 0 && (
                          <div className="mt-3 d-flex flex-column gap-2">
                            {s.resultFiles.map((file) => (
                              <div
                                key={file.id}
                                className="d-flex align-items-center justify-content-between p-2 border rounded bg-white shadow-sm"
                              >
                                <span
                                  className="text-truncate small me-2 text-dark"
                                  style={{ maxWidth: "70%" }}
                                >
                                  {file.originalFileName}{" "}
                                  <span className="text-muted">
                                    ({Math.round(file.fileSize / 1024)} KB)
                                  </span>
                                </span>
                                <button
                                  className="btn btn-outline-primary btn-sm py-1 px-3 fw-bold"
                                  onClick={() => handleDownloadFile(file)}
                                  style={{ fontSize: "0.8rem" }}
                                >
                                  Tải xuống
                                </button>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>

                      {s.status === "SUBMITTED" && (
                        <div className="d-flex gap-2 mt-3">
                          <button
                            className="btn btn-success btn-sm flex-fill"
                            onClick={() => handleReview(s, "APPROVED")}
                          >
                            Duyệt
                          </button>
                          <button
                            className="btn btn-danger btn-sm flex-fill"
                            onClick={() =>
                              handleReview(s, "REVISION_REQUESTED")
                            }
                          >
                            Yêu cầu sửa lại
                          </button>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>
            <div className="card-body p-0">
              <table className="table table-hover mb-0">
                <thead className="table-light">
                  <tr>
                    <th className="ps-3">Trang</th>
                    <th>Assistant</th>
                    <th>Type</th>
                    <th>Title</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="text-center text-muted py-5">
                        {selectedChapterId
                          ? "Chapter này hiện chưa có công việc nào."
                          : "Vui lòng chọn Series và Chapter ở bên trái để xem Task."}
                      </td>
                    </tr>
                  ) : (
                    tasks.map((task) => (
                      <tr key={task.id || task.taskId}>
                        <td className="ps-3 fw-bold">
                          Trang {task.pageNumber || "-"}
                        </td>
                        <td>{task.assistantName}</td>
                        <td>
                          <span className="badge bg-secondary">
                            {task.taskType}
                          </span>
                        </td>
                        <td>{task.title}</td>
                        <td>
                          <span
                            className={`badge ${task.status === "ASSIGNED" ? "bg-primary" : "bg-success"}`}
                          >
                            {task.status}
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
