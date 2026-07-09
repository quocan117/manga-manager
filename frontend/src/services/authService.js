import api from "./api";
export const login = async (form) => {
    const response = await api.post("/auth/login", form);
    return response.data;
};
export const register = async (registerData) => {
    const response = await api.post("/auth/register", registerData);
    return response.data;
};
