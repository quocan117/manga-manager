import { useEffect, useState } from "react";
import { getNotifications } from "../../services/mangakaService";

export default function Notification() {

    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {

        const fetchNotifications = async () => {

            try {

                const data =
                    await getNotifications();

                setNotifications(data);

            } catch (error) {

                console.error(error);

            } finally {

                setLoading(false);

            }

        };

        fetchNotifications();

    }, []);

    if (loading) {

        return (
            <div className="text-center mt-4">
                Loading...
            </div>
        );

    }

    return (

        <div className="card shadow border-0">

            <div className="card-header bg-white">
                <h5 className="mb-0">
                    Notifications
                </h5>
            </div>

            <div className="card-body">

                {
                    notifications.length === 0 && (

                        <div className="text-center text-muted">
                            Không có thông báo nào
                        </div>

                    )
                }

                {
                    notifications.map((notification) => (

                        <div
                            key={notification.id}
                            className={`border rounded p-3 mb-3 ${
                                notification.isRead
                                    ? "bg-light"
                                    : "bg-warning-subtle"
                            }`}
                        >

                            <div className="d-flex justify-content-between">

                                <span
                                    className={`badge ${
                                        notification.type === "APPROVED"
                                            ? "bg-success"
                                            : notification.type === "REJECTED"
                                            ? "bg-danger"
                                            : notification.type === "REVIEW"
                                            ? "bg-warning text-dark"
                                            : "bg-primary"
                                    }`}
                                >
                                    {notification.type}
                                </span>

                                <small className="text-muted">
                                    {
                                        new Date(
                                            notification.createdAt
                                        ).toLocaleString()
                                    }
                                </small>

                            </div>

                            <p className="mt-2 mb-0">
                                {notification.message}
                            </p>

                        </div>

                    ))
                }

            </div>

        </div>

    );
}