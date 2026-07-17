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
  };
  const response = await api.post("/editorial-board/users", payload);
  return response.data;
};

export const cancelSeries = async (seriesId) => {
  const response = await api.put(`/editorial-board/series/${seriesId}/cancel`, {
    decisionType: "CANCEL",
    reason: "Chỉ số tương tác (Likes) rớt xuống mức cảnh báo.",
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

export const importReaderFeedback = async (period, periodStart, periodEnd) => {
  const response = await api.post("/editorial-board/reader-feedback-imports", {
    period,
    periodStart,
    periodEnd,
  });
  return response.data;
};

export const getReaderFeedbackImports = async () => {
  const response = await api.get("/editorial-board/reader-feedback-imports");
  return response.data;
};

export const getRankings = async (period) => {
  const response = await api.get("/editorial-board/rankings", {
    params: period ? { period } : {},
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
