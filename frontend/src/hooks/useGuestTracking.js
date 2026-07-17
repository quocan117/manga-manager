import { useEffect } from "react";
import { trackGuestAccess } from "../services/guestService";

export default function useGuestTracking() {
  useEffect(() => {
    const run = async () => {
      let sessionToken = localStorage.getItem("guest_session_token");
      if (!sessionToken) {
        sessionToken = crypto.randomUUID();
        localStorage.setItem("guest_session_token", sessionToken);
      }
      try {
        await trackGuestAccess(sessionToken);
        console.log("Đã ghi nhận khách truy cập thành công!");
      } catch (error) {
        console.warn("API truy cập bị lỗi  chưa sẵn sàng.", error.message);
      }
    };
    run();
  }, []);
}
