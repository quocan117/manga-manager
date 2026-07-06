import api from "./api";
export const submitChapterToEditor = async (chapterId, manuscriptUrl) => {
  const response = await api.patch(
    `/mangaka/chapters/${chapterId}/submit-to-editor`,
    { manuscriptUrl },
  );
  return response.data;
  console.warn("[TODO-BE] submit-to-editor chưa có API thật, đang trả mock.");
  return { id: chapterId, status: "SUBMITTED_TO_EDITOR", manuscriptUrl };
};
export const getChapterRevisionNotes = async (chapterId) => {
  const response = await api.get(
    `/mangaka/chapters/${chapterId}/revision-notes`,
  );
  return response.data;
  console.warn(
    "[TODO-BE] get revision-notes chưa có API thật, đang trả mảng rỗng.",
  );
  return [];
};
export const getPendingReviewChapters = async () => {
  const response = await api.get("/tantou-editor/chapters/pending-review");
  return response.data;
  console.warn(
    "[TODO-BE] pending-review chapters chưa có API thật, đang trả mảng rỗng.",
  );
  return [];
};
export const getChapterForReview = async (chapterId) => {
  const response = await api.get(`/tantou-editor/chapters/${chapterId}`);
  return response.data;
  console.warn(
    "[TODO-BE] get chapter detail (tantou) chưa có API thật, đang trả null.",
  );
  return null;
};
function dataUrlToBlob(dataUrl) {
  const [header, base64] = dataUrl.split(",");
  const mime = header.match(/:(.*?);/)?.[1] || "image/png";
  const binary = atob(base64);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
  return new Blob([bytes], { type: mime });
}
export const saveChapterRevisionNote = async (
  chapterId,
  { previewImageUrl, canvasData, orderIndex },
) => {
  const formData = new FormData();
  const blob = dataUrlToBlob(previewImageUrl);
  formData.append("image", blob, `revision-note-${orderIndex}.png`);
  formData.append("canvasData", JSON.stringify(canvasData));
  formData.append("orderIndex", orderIndex);
  const response = await api.post(
    `/tantou-editor/chapters/${chapterId}/revision-notes`,
    formData,
    { headers: { "Content-Type": "multipart/form-data" } },
  );
  return response.data;
};
export const sendChapterRevisionToMangaka = async (chapterId) => {
  const response = await api.post(
    `/tantou-editor/chapters/${chapterId}/request-revision`,
  );
  return response.data;
  console.warn(
    "[TODO-BE] request-revision (chapter) chưa có API thật, đang trả mock.",
  );
  return { id: chapterId, status: "REVISION_REQUESTED" };
};
export const publishChapter = async (chapterId) => {
  const response = await api.post(
    `/tantou-editor/chapters/${chapterId}/publish`,
  );
  return response.data;
  console.warn("[TODO-BE] publish chapter chưa có API thật, đang trả mock.");
  return { id: chapterId, status: "PUBLISHED" };
};