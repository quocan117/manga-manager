import api from "./api";

export const getMyTasks = async () => {
  const response = await api.get("/assistant/tasks");
  return response.data;
};

export const getTask = async (taskId) => {
  const response = await api.get(`/assistant/tasks/${taskId}`);
  return response.data;
};

export const acceptTask = async (taskId) => {
  const response = await api.patch(`/assistant/tasks/${taskId}/accept`);
  return response.data;
};

export const getTaskDrawing = async (taskId) => {
  const response = await api.get(`/assistant/tasks/${taskId}/drawing`);
  return response.data;
};

export const saveTaskDrawing = async (
  taskId,
  canvasData,
  previewImageUrl,
  expectedVersion = 0,
) => {
  const response = await api.put(`/assistant/tasks/${taskId}/drawing`, {
    canvasData,
    previewImageUrl,
    expectedVersion,
  });
  return response.data;
};

export const finalizeTaskDrawing = async (taskId, expectedVersion) => {
  const response = await api.post(
    `/assistant/tasks/${taskId}/drawing/finalize`,
    {
      expectedVersion,
    },
  );
  return response.data;
};

export const getTaskDrawingRevisions = async (taskId) => {
  const response = await api.get(
    `/assistant/tasks/${taskId}/drawing/revisions`,
  );
  return response.data;
};

export const restoreTaskDrawingRevision = async (
  taskId,
  revisionId,
  expectedVersion,
) => {
  const response = await api.post(
    `/assistant/tasks/${taskId}/drawing/revisions/${revisionId}/restore`,
    { expectedVersion },
  );
  return response.data;
};

export const getTaskSubmissions = async (taskId) => {
  const response = await api.get(`/assistant/tasks/${taskId}/submissions`);
  return response.data;
};

export const getNotifications = async () => {
  const response = await api.get("/assistant/notifications");
  return response.data;
};

export const markNotificationRead = async (notificationId) => {
  const response = await api.patch(
    `/assistant/notifications/${notificationId}/read`,
  );
  return response.data;
};

export const submitTask = async (
  taskId,
  resultFiles,
  note,
  expectedDrawingVersion,
) => {
  const formData = new FormData();
  if (note) formData.append("note", note);
  if (expectedDrawingVersion !== undefined && expectedDrawingVersion !== null) {
    formData.append("expectedDrawingVersion", expectedDrawingVersion);
  }
  resultFiles.forEach((file) => formData.append("resultFiles", file));
  const response = await api.post(
    `/assistant/tasks/${taskId}/submissions`,
    formData,
  );
  return response.data;
};