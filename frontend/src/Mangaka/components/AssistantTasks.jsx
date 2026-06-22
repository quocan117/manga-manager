import {
    useEffect,
    useState
} from "react";

import {
    getAssistants,
    assignTask,
    getChapterTasks
} from "../../services/mangakaService";

export default function AssistantTasks() {

    const [assistants, setAssistants] =
        useState([]);

    const [tasks, setTasks] =
        useState([]);

    const [chapterId, setChapterId] =
        useState("");

    const [form, setForm] =
        useState({
            pageId: "",
            assistantId: "",
            taskType: "BACKGROUND",
            title: "",
            description: "",
            dueDate: ""
        });

    useEffect(() => {

        fetchAssistants();

    }, []);

    const fetchAssistants =
        async () => {

            try {

                const data =
                    await getAssistants();

                setAssistants(data);

            } catch (error) {

                console.error(error);

            }

        };

    const fetchTasks =
        async () => {

            if (!chapterId) {
                return;
            }

            try {

                const data =
                    await getChapterTasks(
                        chapterId
                    );

                setTasks(data);

            } catch (error) {

                console.error(error);

            }

        };

    const handleChange =
        (e) => {

            setForm({
                ...form,
                [e.target.name]:
                    e.target.value
            });

        };

    const handleAssign =
        async (e) => {

            e.preventDefault();

            try {

                await assignTask({
                    ...form,
                    pageId:
                        Number(
                            form.pageId
                        ),
                    assistantId:
                        Number(
                            form.assistantId
                        )
                });

                alert(
                    "Assign thành công"
                );

                fetchTasks();

            } catch (error) {

                console.error(error);

                alert(
                    "Assign thất bại"
                );

            }

        };

    return (

        <div>

            <h2 className="mb-4">
                Assistant Tasks
            </h2>

            <div className="row">

                {/* Form */}

                <div className="col-md-5">

                    <div className="card shadow">

                        <div className="card-header">
                            Assign Task
                        </div>

                        <div className="card-body">

                            <form
                                onSubmit={
                                    handleAssign
                                }
                            >

                                <div className="mb-3">

                                    <label>
                                        Page ID
                                    </label>

                                    <input
                                        type="number"
                                        name="pageId"
                                        className="form-control"
                                        value={
                                            form.pageId
                                        }
                                        onChange={
                                            handleChange
                                        }
                                        required
                                    />

                                </div>

                                <div className="mb-3">

                                    <label>
                                        Assistant
                                    </label>

                                    <select
                                        name="assistantId"
                                        className="form-select"
                                        value={
                                            form.assistantId
                                        }
                                        onChange={
                                            handleChange
                                        }
                                    >

                                        <option value="">
                                            Select Assistant
                                        </option>

                                        {
                                            assistants.map(
                                                (
                                                    assistant
                                                ) => (

                                                    <option
                                                        key={
                                                            assistant.id
                                                        }
                                                        value={
                                                            assistant.id
                                                        }
                                                    >
                                                        {
                                                            assistant.username
                                                        }
                                                    </option>

                                                )
                                            )
                                        }

                                    </select>

                                </div>

                                <div className="mb-3">

                                    <label>
                                        Task Type
                                    </label>

                                    <select
                                        name="taskType"
                                        className="form-select"
                                        value={
                                            form.taskType
                                        }
                                        onChange={
                                            handleChange
                                        }
                                    >

                                        <option>
                                            BACKGROUND
                                        </option>

                                        <option>
                                            TEXT
                                        </option>

                                        <option>
                                            EFFECTS
                                        </option>

                                        <option>
                                            OTHER
                                        </option>

                                    </select>

                                </div>

                                <div className="mb-3">

                                    <label>
                                        Title
                                    </label>

                                    <input
                                        type="text"
                                        name="title"
                                        className="form-control"
                                        value={
                                            form.title
                                        }
                                        onChange={
                                            handleChange
                                        }
                                    />

                                </div>

                                <div className="mb-3">

                                    <label>
                                        Description
                                    </label>

                                    <textarea
                                        name="description"
                                        className="form-control"
                                        rows="3"
                                        value={
                                            form.description
                                        }
                                        onChange={
                                            handleChange
                                        }
                                    />

                                </div>

                                <div className="mb-3">

                                    <label>
                                        Due Date
                                    </label>

                                    <input
                                        type="date"
                                        name="dueDate"
                                        className="form-control"
                                        value={
                                            form.dueDate
                                        }
                                        onChange={
                                            handleChange
                                        }
                                    />

                                </div>

                                <button
                                    className="btn btn-success w-100"
                                >
                                    Assign Task
                                </button>

                            </form>

                        </div>

                    </div>

                </div>

                {/* Task List */}

                <div className="col-md-7">

                    <div className="card shadow">

                        <div className="card-header d-flex justify-content-between">

                            <span>
                                Current Tasks
                            </span>

                            <div className="d-flex">

                                <input
                                    type="number"
                                    className="form-control me-2"
                                    placeholder="Chapter ID"
                                    value={
                                        chapterId
                                    }
                                    onChange={
                                        (
                                            e
                                        ) =>
                                            setChapterId(
                                                e
                                                    .target
                                                    .value
                                            )
                                    }
                                />

                                <button
                                    className="btn btn-primary"
                                    onClick={
                                        fetchTasks
                                    }
                                >
                                    Load
                                </button>

                            </div>

                        </div>

                        <div className="card-body">

                            <table className="table table-hover">

                                <thead>

                                    <tr>

                                        <th>
                                            Assistant
                                        </th>

                                        <th>
                                            Type
                                        </th>

                                        <th>
                                            Title
                                        </th>

                                        <th>
                                            Status
                                        </th>

                                    </tr>

                                </thead>

                                <tbody>

                                    {
                                        tasks.map(
                                            task => (

                                                <tr
                                                    key={
                                                        task.taskId
                                                    }
                                                >

                                                    <td>
                                                        {
                                                            task.assistantName
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            task.taskType
                                                        }
                                                    </td>

                                                    <td>
                                                        {
                                                            task.title
                                                        }
                                                    </td>

                                                    <td>

                                                        <span className="badge bg-primary">

                                                            {
                                                                task.status
                                                            }

                                                        </span>

                                                    </td>

                                                </tr>

                                            )
                                        )
                                    }

                                </tbody>

                            </table>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    );

}