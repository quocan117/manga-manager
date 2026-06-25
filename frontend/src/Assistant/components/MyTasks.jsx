import { useEffect, useState } from "react";

export default function MyTasks() {

    const [tasks, setTasks] = useState([]);

    useEffect(() => {

        loadTasks();

    }, []);

    const loadTasks = async () => {

        try {

            throw new Error();

        } catch {

            setTasks([
                {
                    id: 1,
                    title: "Draw Main Character",
                    description:
                        "Character page 1",
                    status: "PENDING"
                },
                {
                    id: 2,
                    title: "Ink Background",
                    description:
                        "Page 3",
                    status: "IN_PROGRESS"
                }
            ]);

        }

    };

    return (

        <div>

            <h2>
                My Tasks
            </h2>

            <div className="row">

                {tasks.map(task => (

                    <div
                        key={task.id}
                        className="col-md-6 mb-3"
                    >

                        <div className="card shadow">

                            <div className="card-body">

                                <h5>
                                    {task.title}
                                </h5>

                                <p>
                                    {
                                        task.description
                                    }
                                </p>

                                <span
                                    className="badge bg-primary"
                                >
                                    {task.status}
                                </span>

                            </div>

                        </div>

                    </div>

                ))}

            </div>

        </div>

    );

}