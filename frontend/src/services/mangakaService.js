import axios from "axios";

const API_URL =
    "http://localhost:8080";

const authHeader = () => ({
    Authorization:
        `Bearer ${localStorage.getItem("token")}`
});

export const getMySeries =
    async () => {

        const response =
            await axios.get(
                `${API_URL}/mangaka/my-series`,
                {
                    headers:
                        authHeader()
                }
            );

        return response.data;

    };

export const getSeriesChapters =
    async (seriesId) => {

        const response =
            await axios.get(
                `${API_URL}/mangaka/series/${seriesId}/chapters`,
                {
                    headers:
                        authHeader()
                }
            );

        return response.data;

    };