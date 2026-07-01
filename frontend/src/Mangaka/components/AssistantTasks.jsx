import { useEffect, useState } from "react";
import {
    getAssistants,
    assignTask,
    getChapterTasks,
    getMySeries,
    getSeriesChapters,
    getChapterPages
} from "../../services/mangakaService";

export default function AssistantTasks() {
    // 1. Dữ liệu danh sách (Dropdowns)
    const [assistants, setAssistants] = useState([]);
    const [seriesList, setSeriesList] = useState([]);
    const [chapterList, setChapterList] = useState([]);
    const [pageList, setPageList] = useState([]);
    const [tasks, setTasks] = useState([]);

    // 2. State điều hướng cấp bậc
    const [selectedSeriesId, setSelectedSeriesId] = useState("");
    const [selectedChapterId, setSelectedChapterId] = useState("");

    // 3. Data gửi đi (Form)
    const [form, setForm] = useState({
        pageId: "",
        assistantId: "",
        taskType: "BACKGROUND",
        title: "",
        description: "",
        dueDate: ""
    });

    // ----------------------------------------------------
    // HOOKS & TẢI DỮ LIỆU
    // ----------------------------------------------------

    // Tải danh sách Trợ lý và Series khi mới vào trang
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

    // Tự động tải Chapter khi chọn Series
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

    // Tự động tải Pages & Danh sách Task khi chọn Chapter
    useEffect(() => {
        if (selectedChapterId) {
            fetchTasks();
            fetchPages();
        } else {
            setTasks([]);
            setPageList([]);
            setForm((prev) => ({ ...prev, pageId: "" }));
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
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

    const handleChange = (e) => {
        setForm({
            ...form,
            [e.target.name]: e.target.value
        });
    };

    const handleAssign = async (e) => {
        e.preventDefault();
        try {
            await assignTask({
                ...form,
                pageId: Number(form.pageId),
                assistantId: Number(form.assistantId),
                areaX: 0,
                areaY: 0,
                areaWidth: 100,
                areaHeight: 100
            });

            alert("Giao việc cho trợ lý thành công!");
            setForm({ ...form, title: "", description: "", pageId: "" }); 
            fetchTasks(); 
        } catch (error) {
            console.error(error);
            alert("Giao việc thất bại. Vui lòng kiểm tra lại dữ liệu.");
        }
    };

    const selectedChapterData = chapterList.find(c => c.id === Number(selectedChapterId));

    return (
        <div>
            <h2 className="mb-4">Assistant Tasks</h2>

            <div className="row">
                <div className="col-md-5">
                    <div className="card shadow mb-4">
                        <div className="card-header bg-white fw-bold">
                            Assign Task
                        </div>
                        <div className="card-body">
                            <form onSubmit={handleAssign}>

                                <div className="mb-3">
                                    <label className="form-label text-primary fw-bold">1. Chọn Series</label>
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
                                    <label className="form-label text-primary fw-bold">2. Chọn Chapter</label>
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
                                    <label className="form-label text-primary fw-bold">3. Chọn Trang Truyện (Page)</label>
                                    <select
                                        name="pageId"
                                        className="form-select border-primary"
                                        value={form.pageId}
                                        onChange={handleChange}
                                        required
                                        disabled={pageList.length === 0}
                                    >
                                        <option value="">
                                            {pageList.length === 0 ? "Vui lòng chọn Chapter trước" : "--- Chọn Trang để giao việc ---"}
                                        </option>
                                        {pageList.map((page) => (
                                            <option key={page.id} value={page.id}>
                                                Trang {page.pageNumber}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <hr className="my-4"/>

                                <div className="mb-3">
                                    <label className="form-label">Chọn Trợ lý (Assistant)</label>
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
                                    <label className="form-label">Loại Công Việc (Task Type)</label>
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
                                    <label className="form-label">Tiêu đề (Title)</label>
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
                                    <label className="form-label">Mô tả chi tiết (Description)</label>
                                    <textarea
                                        name="description"
                                        className="form-control"
                                        rows="3"
                                        value={form.description}
                                        onChange={handleChange}
                                    />
                                </div>

                                <div className="mb-4">
                                    <label className="form-label">Hạn chót (Due Date)</label>
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
                                    Assign Task
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <div className="col-md-7">
                    <div className="card shadow">
                        <div className="card-header bg-white py-3">
                            <span className="fw-bold fs-5">
                                Current Tasks {selectedChapterData && `- Chapter ${selectedChapterData.chapterNumber}`}
                            </span>
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
                                                {selectedChapterId ? "Chapter này hiện chưa có công việc nào." : "Vui lòng chọn Series và Chapter ở bên trái để xem Task."}
                                            </td>
                                        </tr>
                                    ) : (
                                        tasks.map((task) => (
                                            <tr key={task.id || task.taskId}>
                                                <td className="ps-3 fw-bold">Trang {task.pageNumber || "-"}</td>
                                                <td>{task.assistantName}</td>
                                                <td>
                                                    <span className="badge bg-secondary">{task.taskType}</span>
                                                </td>
                                                <td>{task.title}</td>
                                                <td>
                                                    <span className={`badge ${task.status === "ASSIGNED" ? "bg-primary" : "bg-success"}`}>
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