import { useEffect, useState } from "react";

export default function DashboardAssistant() {

    const [tasks, setTasks] = useState([]);
    const [submissions, setSubmissions] = useState([]);

    useEffect(() => {

        loadData();

    }, []);

    const loadData = async () => {

        try {

            throw new Error("Backend chưa xong");

        } catch {

            setTasks([
                {
                    id: 1,
                    title: "Draw Character",
                    status: "IN_PROGRESS"
                },
                {
                    id: 2,
                    title: "Background Sketch",
                    status: "PENDING"
                }
            ]);

            setSubmissions([
                {
                    id: 1,
                    status: "APPROVED"
                }
            ]);

        }

    };

    return (

        <div>

            <h2 className="mb-4">
                Assistant Dashboard
            </h2>

            <div className="row">

                <div className="col-md-4">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6>Total Tasks</h6>

                            <h2>{tasks.length}</h2>

                        </div>

                    </div>

                </div>

                <div className="col-md-4">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6>Submissions</h6>

                            <h2>{submissions.length}</h2>

                        </div>

                    </div>

                </div>

                <div className="col-md-4">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6>Pending Tasks</h6>

                            <h2>
                                {
                                    tasks.filter(
                                        t =>
                                            t.status ===
                                            "PENDING"
                                    ).length
                                }
                            </h2>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    );

}