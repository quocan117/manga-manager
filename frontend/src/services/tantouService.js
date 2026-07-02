import api from "./api";

export const getStudioProgress = async () => {
  const response = await api.get("/tantou-editor/studio/progress");
  return response.data;
};

export const getPendingReviewSeries = async () => {
  const response = await api.get(
    "/tantou-editor/series/pending-editorial-review",
  );
  return response.data;
};

export const getSeriesDossier = async (seriesId) => {
  const response = await api.get(`/tantou-editor/series/${seriesId}/dossier`);
  return response.data;
};

export const submitToBoard = async (seriesId, note) => {
  const response = await api.patch(
    `/tantou-editor/series/${seriesId}/submit-to-board`,
    { note },
  );
  return response.data;
};

export const requestRevision = async (seriesId, note) => {
  const response = await api.patch(
    `/tantou-editor/series/${seriesId}/request-revision`,
    { note },
  );
  return response.data;
};

export const getSchedules = async () => {
  const response = await api.get("/tantou-editor/schedules");
  return response.data;
};

export const createSchedule = async (scheduleData) => {
  const response = await api.post("/tantou-editor/schedules", scheduleData);
  return response.data;
};

export const getNotifications = async () => {
  const response = await api.get("/tantou-editor/notifications");
  return response.data;
};

export const markNotificationRead = async (notificationId) => {
  const response = await api.patch(`/notifications/${notificationId}/read`);
  return response.data;
};

export const acceptSeries = async (seriesId) => {
  const response = await api.post(`/tantou-editor/series/${seriesId}/accept`);
  return response.data;
};
