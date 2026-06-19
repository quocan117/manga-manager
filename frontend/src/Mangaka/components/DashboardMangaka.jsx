import { useEffect, useState } from "react";
import {
    getMySeries
} from "../../services/mangakaService";
import {
    getNotifications
} from "../../services/notificationService";

export default function DashboardMangaka() {

    const [series, setSeries] = useState([]);
    const [notifications, setNotifications] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    useEffect(() => {

        fetchDashboard();

    }, []);

    const fetchDashboard = async () => {

        try {

            setLoading(true);

            const [
                seriesData,
                notificationData
            ] = await Promise.all([
                getMySeries(),
                getNotifications()
            ]);

            setSeries(seriesData || []);
            setNotifications(
                notificationData || []
            );

        } catch (error) {

            console.error(error);

        } finally {

            setLoading(false);

        }

    };

    const totalSeries =
        series.length;

    const publishedSeries =
        series.filter(
            item =>
                item.status ===
                "Published"
        ).length;

    const reviewingSeries =
        series.filter(
            item =>
                item.status ===
                "Reviewing"
        ).length;

    const averageScore =
        series.length > 0
            ? (
                  series.reduce(
                      (sum, item) =>
                          sum +
                          (item.rankingScore ||
                              0),
                      0
                  ) / series.length
              ).toFixed(1)
            : 0;

    if (loading) {

        return (

            <div className="text-center mt-5">

                <div
                    className="spinner-border"
                />

                <p className="mt-3">
                    Loading...
                </p>

            </div>

        );

    }

    return (

        <div>

            <h2 className="mb-4">
                Dashboard
            </h2>

            {/* Statistics */}

            <div className="row g-4">

                <div className="col-md-3">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6 className="text-muted">
                                Total Series
                            </h6>

                            <h2>
                                {totalSeries}
                            </h2>

                        </div>

                    </div>

                </div>

                <div className="col-md-3">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6 className="text-muted">
                                Published
                            </h6>

                            <h2 className="text-success">
                                {publishedSeries}
                            </h2>

                        </div>

                    </div>

                </div>

                <div className="col-md-3">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6 className="text-muted">
                                Reviewing
                            </h6>

                            <h2 className="text-warning">
                                {reviewingSeries}
                            </h2>

                        </div>

                    </div>

                </div>

                <div className="col-md-3">

                    <div className="card shadow">

                        <div className="card-body text-center">

                            <h6 className="text-muted">
                                Avg Ranking
                            </h6>

                            <h2 className="text-primary">
                                {averageScore}
                            </h2>

                        </div>

                    </div>

                </div>

            </div>

            {/* Recent Series */}

            <div className="card shadow mt-4">

                <div className="card-header">
                    My Recent Series
                </div>

                <div className="card-body">

                    {series.length === 0 ? (

                        <p className="text-muted">
                            Chưa có series nào.
                        </p>

                    ) : (

                        <table className="table">

                            <thead>

                                <tr>

                                    <th>
                                        Title
                                    </th>

                                    <th>
                                        Status
                                    </th>

                                    <th>
                                        Score
                                    </th>

                                </tr>

                            </thead>

                            <tbody>

                                {series.map(item => (

                                    <tr
                                        key={item.id}
                                    >

                                        <td>
                                            {
                                                item.title
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.status
                                            }
                                        </td>

                                        <td>
                                            {
                                                item.rankingScore ??
                                                0
                                            }
                                        </td>

                                    </tr>

                                ))}

                            </tbody>

                        </table>

                    )}

                </div>

            </div>

            {/* Notifications */}

            <div className="card shadow mt-4">

                <div className="card-header">
                    Notifications
                </div>

                <div className="card-body">

                    {notifications.length ===
                    0 ? (

                        <p className="text-muted">
                            Không có thông báo.
                        </p>

                    ) : (

                        <ul className="list-group">

                            {notifications.map(
                                notification => (

                                    <li
                                        key={
                                            notification.id
                                        }
                                        className="list-group-item"
                                    >

                                        {
                                            notification.message
                                        }

                                    </li>

                                )
                            )}

                        </ul>

                    )}

                </div>

            </div>

        </div>

    );

}