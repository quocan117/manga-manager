import axios from "axios";

const API_URL = "http://localhost:8080/api";

export const login = async (form) => {
    const response = await fetch(
        "http://localhost:8080/auth/login",
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(form),
        }
    );

    if (!response.ok) {
        throw new Error("Login failed");
    }

    return await response.json();
};

export const register = async (registerData) => {
    const response = await axios.post(
        `${API_URL}/register`,
        registerData
    );

    return response.data;
};