// Áp dụng Fetch API (Exercise 10) cho toàn bộ service, thay cho axios.
import api from "./api";
const BASE_URL = "http://localhost:8080";
// Guest like: không cần token nên gọi fetch trực tiếp, giữ nguyên logic gốc
export const likeChapter = async (chapterId, sessionToken) => {
  const response = await fetch(`${BASE_URL}/chapters/${chapterId}/likes`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ sessionToken: sessionToken }),
  });
  return response.ok;
};
export const chapterService = {
  createChapter: async (chapterData) => {
    const response = await api.post("/mangaka/chapters", chapterData);
    return response.data;
  },
  uploadChapterPages: async (chapterId, files) => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append("images", file);
    });
    const response = await api.post(
      `/mangaka/chapters/${chapterId}/pages`,
      formData,
    );
    return response.data;
  },
};
