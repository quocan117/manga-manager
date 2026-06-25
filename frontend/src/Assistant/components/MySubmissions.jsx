import { useEffect, useState } from "react";

export default function MySubmissions() {

    const [submissions,
        setSubmissions] =
        useState([]);

    useEffect(() => {

        loadSubmissions();

    }, []);

    const loadSubmissions =
        async () => {

            try {

                throw new Error();

            } catch {

                setSubmissions([
                    {
                        id: 1,
                        status: "APPROVED",
                        reviewNote:
                            "Good work"
                    },
                    {
                        id: 2,
                        status: "PENDING",
                        reviewNote: null
                    }
                ]);

            }

        };

    return (

        <div>

            <h2>
                My Submissions
            </h2>

            <table className="table">

                <thead>

                    <tr>

                        <th>ID</th>
                        <th>Status</th>
                        <th>Review</th>

                    </tr>

                </thead>

                <tbody>

                    {
                        submissions.map(
                            item => (

                                <tr
                                    key={item.id}
                                >

                                    <td>
                                        {item.id}
                                    </td>

                                    <td>
                                        {item.status}
                                    </td>

                                    <td>
                                        {
                                            item.reviewNote ||
                                            "-"
                                        }
                                    </td>

                                </tr>

                            )
                        )
                    }

                </tbody>

            </table>

        </div>

    );

}