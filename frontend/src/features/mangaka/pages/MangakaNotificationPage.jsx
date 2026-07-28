import NotificationBoard from "../../../components/notifications/NotificationBoard";
import * as mangakaService from "../../../services/mangakaService";

const getBadgeClass = (type) => {
  switch (type) {
    case "APPROVED":
      return "bg-success";
    case "REJECTED":
      return "bg-danger";
    case "REVIEW":
      return "bg-warning text-dark";
    case "SERIES_RANKING_AT_RISK":
      return "bg-danger";
    default:
      return "bg-primary";
  }
};

export default function MangakaNotificationPage() {
  return (
    <NotificationBoard
      title="Notifications"
      service={mangakaService}
      getBadgeClass={getBadgeClass}
    />
  );
}
