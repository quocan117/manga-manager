import api from "./api";

export const getUsers = async () => {
    const response = await api.get("/editorial-board/users");
    return response.data;
};

export const createMangaka = async (userData) => {
    const payload = {
        ...userData,
        role: "MANGAKA",
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