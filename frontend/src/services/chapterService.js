import axios from "axios";
const BASE_URL = "http://localhost:8080";

const authHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem("token")}`,
});

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
    const response = await axios.post(
      `${BASE_URL}/mangaka/chapters`,
      chapterData,
      {
        headers: authHeader(),
      },
    );
    return response.data;
  },

  uploadChapterPages: async (chapterId, files) => {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append("images", file);
    });

    const response = await axios.post(
      `${BASE_URL}/mangaka/chapters/${chapterId}/pages`,
      formData,
      {
        headers: {
          ...authHeader(),
          "Content-Type": "multipart/form-data",
        },
      },
    );
    return response.data;
  },
};
