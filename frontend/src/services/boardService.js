import api from "./api";

export const getUsers = async () => {
  const response = await api.get("/editorial-board/users");
  return response.data;
};

export const createUser = async (userData) => {
  const payload = {
    username: userData.username,
    email: userData.email,
    password: userData.password,
    role: userData.role,
    status: "ACTIVE",
    specialty: userData.specialty,
  };
  const response = await api.post("/editorial-board/users", payload);
  return response.data;
};

export const cancelSeries = async (
  seriesId,
  reason = "Chỉ số tương tác (Likes) rớt xuống mức cảnh báo.",
) => {
  const response = await api.put(`/editorial-board/series/${seriesId}/cancel`, {
    decisionType: "CANCEL",
    reason,
  });
  return response.data;
};

export const updateUserStatus = async (userId, newStatus) => {
  const response = await api.put(`/editorial-board/users/${userId}`, {
    status: newStatus,
  });
  return response.data;
};

export const deleteMangaka = async (userId) => {
  const response = await api.delete(`/editorial-board/users/${userId}`);
  return response.data;
};

export const getReviewingSeries = async () => {
  const response = await api.get("/editorial-board/series/reviewing");
  return response.data;
};

export const voteSeriesDecision = async (
  seriesId,
  decisionType,
  reason = "",
) => {
  const response = await api.post(
    `/editorial-board/series/${seriesId}/decisions`,
    {
      decisionType: decisionType,
      reason: reason,
    },
  );
  return response.data;
};

export const getPublishSchedules = async () => {
  const response = await api.get("/editorial-board/publish-schedules");
  return response.data;
};

export const getReaderVoteSummary = async (from, to) => {
  const response = await api.get("/editorial-board/reader-votes/summary", {
    params: { from, to },
  });
  return response.data;
};

export const getReaderVotes = async (from, to) => {
  const response = await api.get("/editorial-board/reader-votes", {
    params: { from, to },
  });
  return response.data;
};

export const importReaderFeedback = async (
  seriesId,
  periodStart,
  periodEnd,
) => {
  const response = await api.post("/editorial-board/reader-feedback-imports", {
    seriesId,
    periodStart,
    periodEnd,
  });
  return response.data;
};

export const getReaderFeedbackImports = async () => {
  const response = await api.get("/editorial-board/reader-feedback-imports");
  return response.data;
};

export const getRankings = async (periodStart, periodEnd) => {
  const response = await api.get("/editorial-board/rankings", {
    params: periodStart && periodEnd ? { periodStart, periodEnd } : {},
  });
  return response.data;
};

export const getRankingPeriods = async () => {
  const response = await api.get("/editorial-board/rankings/periods");
  return response.data;
};

export const getSeriesTotalVotes = async () => {
  const response = await api.get("/editorial-board/rankings/total-votes");
  return response.data;
};

export const getNotifications = async () => {
  const response = await api.get("/editorial-board/notifications");
  return response.data;
};

export const markNotificationRead = async (id) => {
  const response = await api.patch(`/editorial-board/notifications/${id}/read`);
  return response.data;
};

export const createPublishSchedule = async (payload) => {
  const response = await api.post(
    "/editorial-board/publish-schedules",
    payload,
  );
  return response.data;
};

export const updatePublishSchedule = async (scheduleId, payload) => {
  const response = await api.put(
    `/editorial-board/publish-schedules/${scheduleId}`,
    payload,
  );
  return response.data;
};

export const getApprovedSeries = async () => {
  const response = await api.get("/editorial-board/series/approved");
  return response.data;
};

export const getSeriesChapters = async (seriesId) => {
  const response = await api.get(
    `/editorial-board/series/${seriesId}/chapters`,
  );
  return response.data;
};

export const getChapterForBoardReview = async (chapterId) => {
  const response = await api.get(`/editorial-board/chapters/${chapterId}`);
  return response.data;
};

export const reviewChapter = async (chapterId, confirmed, comment) => {
  const response = await api.post(
    `/editorial-board/chapters/${chapterId}/review`,
    {
      confirmed,
      comment,
    },
  );
  return response.data;
};

export const getEditorAssignmentRequiredSeries = async () => {
  const response = await api.get(
    "/editorial-board/series/editor-assignment-required",
  );
  return response.data;
};

export const assignEditor = async (seriesId, editorId) => {
  const response = await api.patch(
    `/editorial-board/series/${seriesId}/assign-editor`,
    { editorId },
  );
  return response.data;
};

export const getMyAssignedSeries = async () => {
  const response = await api.get("/editorial-board/series/my-assigned");
  return response.data;
};

export const getSeriesFeedbackHistory = async (seriesId) => {
  const response = await api.get(
    `/editorial-board/series/${seriesId}/feedback-imports`,
  );
  return response.data;
};

export const getAssignmentHistory = async () => {
  const response = await api.get("/editorial-board/assignment-history");
  return response.data;
};

export const getSeriesReview = async (id) => {
  const response = await api.get(`/editorial-board/series/${id}/review`);
  return response.data;
};

export const getDropRequestedSeries = async () => {
  const response = await api.get("/editorial-board/series/drop-requested");
  return response.data;
};