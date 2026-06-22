import axios from "axios";

const API_URL = "http://localhost:8080/auth";

export const login = async (form) => {
    const response = await axios.post(
        `${API_URL}/login`,
        form
    );

    return response.data;
};

export const register = async (registerData) => {
    const response = await axios.post(
        `${API_URL}/register`,
        registerData
    );

    return response.data;
};