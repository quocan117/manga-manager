import { useState } from "react";

export default function Notification() {
    const [notifications] = useState([
            "Chapter 20 đã được duyệt",
            "Manga Dark Hunter đang được review",
            "Có feedback mới từ Editor",
        ]);
    return (
        <div className="card shadow mt-4">
            <div className="card-header">
                Notifications
            </div>

            <div className="card-body">

                <ul>
                    {notifications.map(
                        (notification, index) => (
                            <li key={index}>
                                {notification}
                            </li>
                        )
                    )}
                </ul>

            </div>
        </div>
    );
}