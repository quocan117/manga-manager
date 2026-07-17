import api from "./api";

export const getMySeries = async () => {
  const response = await api.get("/mangaka/my-series");
  return response.data;
};

export const submitSeriesReview = async (seriesId, storyboardUrl) => {
  const response = await api.post(`/mangaka/series/${seriesId}/submit`, {
    storyboardUrl,
  });
  return response.data;
};

export const getSeriesChapters = async (seriesId) => {
  const response = await api.get(`/mangaka/series/${seriesId}/chapters`);
  return response.data;
};

export const createPage = async (data) => {
  const response = await api.post("/mangaka/pages", data);
  return response.data;
};

export const getAssistants = async () => {
  const response = await api.get("/mangaka/assistants");
  return response.data;
};

export const assignTask = async (data) => {
  const response = await api.post("/mangaka/tasks", data);
  return response.data;
};

export const getChapterTasks = async (chapterId) => {
  const response = await api.get(`/mangaka/chapters/${chapterId}/tasks`);
  return response.data;
};

export const getChapterSubmissions = async (chapterId) => {
  const response = await api.get(`/mangaka/chapters/${chapterId}/submissions`);
  return response.data;
};

export const reviewSubmission = async (submissionId, decision, reviewNote) => {
  const response = await api.patch(
    `/mangaka/submissions/${submissionId}/review`,
    { decision, reviewNote },
  );
  return response.data;
};

export const getRankings = async () => {
  const response = await api.get("/mangaka/rankings");
  return response.data;
};

export const getNotifications = async () => {
  const response = await api.get("/mangaka/notifications");
  return response.data;
};

export const markNotificationRead = async (notificationId) => {
  const response = await api.patch(
    `/mangaka/notifications/${notificationId}/read`,
    {},
  );
  return response.data;
};

export const getChapterPages = async (chapterId) => {
  const response = await api.get(`/mangaka/chapters/${chapterId}/pages`);
  return response.data;
};

export const createAssistant = async (assistantData) => {
  const response = await api.post("/mangaka/assistants", assistantData);
  return response.data;
};

export const getChapterById = async (chapterId) => {
  const response = await api.get(`/mangaka/chapters/${chapterId}`);
  return response.data;
};

export const createSeriesWithCoverUpload = async (form, coverImageFile) => {
  const formData = new FormData();
  formData.append("title", form.title);
  form.genres.forEach((genre) => formData.append("genres", genre));
  if (form.description) formData.append("description", form.description);
  if (form.publicationType)
    formData.append("publicationType", form.publicationType);
  if (form.artStyle) formData.append("artStyle", form.artStyle);
  if (coverImageFile) formData.append("coverImage", coverImageFile);
  const response = await api.post("/mangaka/series", formData);
  return response.data;
};

export const updateAssistantStatus = async (assistantId, status) => {
  const response = await api.patch(
    `/mangaka/assistants/${assistantId}/status`,
    { status },
  );
  return response.data;
};

export const deleteAssistant = async (assistantId) => {
  const response = await api.delete(`/mangaka/assistants/${assistantId}`);
  return response.data;
};