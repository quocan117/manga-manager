import NotificationBoard from "../../../components/notifications/NotificationBoard";
import * as assistantService from "../../../services/assistantService";

export default function AssistantNotificationPage() {
  return <NotificationBoard title="Thông báo" service={assistantService} />;
}
