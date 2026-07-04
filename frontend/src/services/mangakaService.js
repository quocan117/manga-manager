import axios from "axios";

const API_URL = "http://localhost:8080/mangaka";

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem("token")}`,
});

const getAuthHeaders = () => {
  const token = localStorage.getItem("token");
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  };
};

export const getMySeries = async () => {
  const response = await axios.get(`${API_URL}/my-series`, {
    headers: authHeader(),
  });

  return response.data;
};

export const submitSeriesReview = async (seriesId, storyboardUrl) => {
  const response = await axios.post(
    `${API_URL}/series/${seriesId}/submit`,
    {
      storyboardUrl,
    },
    {
      headers: authHeader(),
    },
  );

  return response.data;
};

export const getSeriesChapters = async (seriesId) => {
  const response = await axios.get(`${API_URL}/series/${seriesId}/chapters`, {
    headers: authHeader(),
  });

  return response.data;
};

export const createPage = async (data) => {
  const response = await axios.post(`${API_URL}/pages`, data, {
    headers: authHeader(),
  });

  return response.data;
};

export const getAssistants = async () => {
  const response = await axios.get(`${API_URL}/assistants`, getAuthHeaders());
  return response.data;
};

export const assignTask = async (data) => {
  const response = await axios.post(`${API_URL}/tasks`, data, {
    headers: authHeader(),
  });

  return response.data;
};

export const getChapterTasks = async (chapterId) => {
  const response = await axios.get(`${API_URL}/chapters/${chapterId}/tasks`, {
    headers: authHeader(),
  });

  return response.data;
};

export const getChapterSubmissions = async (chapterId) => {
  const response = await axios.get(
    `${API_URL}/chapters/${chapterId}/submissions`,
    {
      headers: authHeader(),
    },
  );

  return response.data;
};

export const reviewSubmission = async (submissionId, decision, reviewNote) => {
  const response = await axios.patch(
    `${API_URL}/submissions/${submissionId}/review`,
    {
      decision,
      reviewNote,
    },
    {
      headers: authHeader(),
    },
  );

  return response.data;
};

export const getRankings = async () => {
  const response = await axios.get(`${API_URL}/rankings`, {
    headers: authHeader(),
  });

  return response.data;
};

export const getNotifications = async () => {
  const response = await axios.get(`${API_URL}/notifications`, {
    headers: authHeader(),
  });

  return response.data;
};

export const markNotificationRead = async (notificationId) => {
  const response = await axios.patch(
    `${API_URL}/notifications/${notificationId}/read`,
    {},
    {
      headers: authHeader(),
    },
  );

  return response.data;
};

export const getChapterPages = async (chapterId) => {
  const response = await axios.get(`${API_URL}/chapters/${chapterId}/pages`, {
    headers: authHeader(),
  });
  return response.data;
};

export const createAssistant = async (assistantData) => {
  const response = await axios.post(
    `${API_URL}/assistants`,
    assistantData,
    getAuthHeaders(),
  );
  return response.data;
};

export const getChapterById = async (chapterId) => {
  const response = await axios.get(`${API_URL}/chapters/${chapterId}`, {
    headers: authHeader(),
  });
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

  const response = await axios.post(`${API_URL}/series`, formData, {
    headers: {
      ...authHeader(),
      "Content-Type": "multipart/form-data",
    },
  });

  return response.data;
};
