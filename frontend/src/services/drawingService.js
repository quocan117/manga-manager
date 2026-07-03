import api from "./api";

export const savePageDrawing = async (
  pageId,
  canvasData,
  previewImageUrl,
  expectedVersion = 0,
) => {
  try {
    const response = await api.put(`/mangaka/pages/${pageId}/drawing`, {
      canvasData,
      previewImageUrl,
      expectedVersion,
    });
    return response.data;
  } catch (error) {
    console.error("Lỗi khi lưu bản đánh dấu:", error);
    throw error;
  }
};

export const getPageDrawing = async (pageId) => {
  const response = await api.get(`/mangaka/pages/${pageId}/drawing`);
  return response.data;
};

export const finalizeDrawing = async (pageId, expectedVersion) => {
  try {
    const response = await api.post(
      `/mangaka/pages/${pageId}/drawing/finalize`,
      {
        expectedVersion: expectedVersion,
      },
    );
    return response.data;
  } catch (error) {
    console.error("Lỗi khi chốt bản vẽ:", error);
    throw error;
  }
};

