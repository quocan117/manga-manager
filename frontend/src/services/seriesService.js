import api from "./api";

const BASE_URL = "http://localhost:8080";

export const getAllSeries = async () => {
  const response = await fetch(`${BASE_URL}/manga-series`);
  if (!response.ok) {
    throw new Error("Lỗi khi tải danh sách truyện từ máy chủ");
  }
  return await response.json();
};

export const getSeriesArchiveHistory = async (seriesId) => {
  const response = await api.get(`/api/series/${seriesId}/archive-history`);
  return response.data;
};