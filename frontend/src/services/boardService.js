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