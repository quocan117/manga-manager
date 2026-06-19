const BASE_URL = "http://localhost:8080";

export const likeChapter = async (chapterId, sessionToken) => {
  const response = await fetch(`${BASE_URL}/chapters/${chapterId}/likes`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ sessionToken: sessionToken }),
  });

  if (!response.ok) throw new Error(`Lỗi từ Backend: ${response.status}`);
  return await response.json();
};
