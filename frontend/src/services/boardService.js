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
        status: "ACTIVE"
    };
    const response = await api.post("/editorial-board/users", payload);
    return response.data;
};
export const cancelSeries = async (seriesId) => {
    const response = await api.put(`/editorial-board/series/${seriesId}/cancel`, {
        decisionType: "CANCEL",
        reason: "Chỉ số tương tác (Likes) rớt xuống mức cảnh báo." 
    });
    return response.data;
};
export const updateUserStatus = async (userId, newStatus) => {
  const response = await api.put(`/editorial-board/users/${userId}`, { status: newStatus });
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
export const voteSeriesDecision = async (seriesId, decisionType, reason = "") => {
    const response = await api.post(`/editorial-board/series/${seriesId}/decisions`, {
        decisionType: decisionType, 
        reason: reason
    });
    return response.data;
};