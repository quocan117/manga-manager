const API_BASE_URL = "http://localhost:8080";

export const trackGuestAccess = async (sessionToken) => {
  await fetch(`${API_BASE_URL}/api/guest/access`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      sessionToken,
      userAgent: navigator.userAgent,
    }),
  });
};
