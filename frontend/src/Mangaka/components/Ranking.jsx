import { useEffect, useState } from "react";
import { getRankings }
    from "../../services/rankingService";

export default function Ranking() {

    const [rankings, setRankings] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    useEffect(() => {

        fetchRankings();

    }, []);

    const fetchRankings = async () => {

        try {

            const data =
                await getRankings();

            setRankings(data);

        } catch (error) {

            console.error(error);

        } finally {

            setLoading(false);

        }

    };

    if (loading) {

        return (
            <div className="text-center mt-5">
                Loading rankings...
            </div>
        );
    }

    return (

        <div className="card shadow">

            <div className="card-header bg-warning">

                <h4 className="mb-0">
                    🏆 Series Rankings
                </h4>

            </div>

            <div className="card-body">

                {
                    rankings.length === 0 && (

                        <div
                            className="alert alert-info"
                        >
                            Chưa có dữ liệu xếp hạng.
                        </div>

                    )
                }

                {
                    rankings.length > 0 && (

                        <table
                            className="table table-hover"
                        >

                            <thead>

                                <tr>

                                    <th>
                                        Rank
                                    </th>

                                    <th>
                                        Series
                                    </th>

                                    <th>
                                        Score
                                    </th>

                                    <th>
                                        Votes
                                    </th>

                                    <th>
                                        Period
                                    </th>

                                    <th>
                                        Updated
                                    </th>

                                </tr>

                            </thead>

                            <tbody>

                                {
                                    rankings.map(
                                        ranking => (

                                            <tr
                                                key={
                                                    ranking.id
                                                }
                                            >

                                                <td>

                                                    {
                                                        ranking.position === 1 &&
                                                        "🥇"
                                                    }

                                                    {
                                                        ranking.position === 2 &&
                                                        "🥈"
                                                    }

                                                    {
                                                        ranking.position === 3 &&
                                                        "🥉"
                                                    }

                                                    {
                                                        ranking.position > 3 &&
                                                        `#${ranking.position}`
                                                    }

                                                </td>

                                                <td>

                                                    {
                                                        ranking.seriesTitle
                                                    }

                                                </td>

                                                <td>

                                                    {
                                                        ranking.score
                                                    }

                                                </td>

                                                <td>

                                                    {
                                                        ranking.voteCount
                                                    }

                                                </td>

                                                <td>

                                                    {
                                                        ranking.period
                                                    }

                                                </td>

                                                <td>

                                                    {
                                                        ranking.calculatedAt
                                                            ? new Date(
                                                                ranking.calculatedAt
                                                            ).toLocaleString()
                                                            : "-"
                                                    }

                                                </td>

                                            </tr>

                                        )
                                    )
                                }

                            </tbody>

                        </table>

                    )
                }

            </div>

        </div>

    );
}