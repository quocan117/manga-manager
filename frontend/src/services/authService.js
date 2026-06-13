import axios from "axios";

const API_URL = "http://localhost:8080/api";

export const login = async (loginData) => {
  const response = await axios.post(
    `${API_URL}/login`,
    loginData
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