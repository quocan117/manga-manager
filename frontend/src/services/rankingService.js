import axios from "axios";

const API_URL = "http://localhost:8080/mangaka";

export const getRankings = async () => {

    const token =
        localStorage.getItem("token");

    const response =
        await axios.get(
            `${API_URL}/rankings`,
            {
                headers: {
                    Authorization:
                        `Bearer ${token}`
                }
            }
        );

    return response.data;
};