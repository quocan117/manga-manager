import api from "./api";

/**
 * Lưu dữ liệu canvas lên backend
 * @param {number} pageId - ID của trang truyện
 * @param {object} canvasData - Dữ liệu chuỗi JSON của Fabric.js
 * @param {string} previewImageUrl - Ảnh base64 preview (tùy chọn)
 * @param {number} expectedVersion - Dùng cho Optimistic Locking (tránh conflict)
 */
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

// import axios from "axios";

// const API_URL = "http://localhost:8080/mangaka";

// const authHeader = () => ({
//     Authorization: `Bearer ${localStorage.getItem("token")}`
// });

// /* ===========================
//    GET DRAWING
// =========================== */

// export const getDrawing = async (pageId) => {

//     const response = await axios.get(
//         `${API_URL}/pages/${pageId}/drawing`,
//         {
//             headers: authHeader()
//         }
//     );

//     return response.data;

// };

// /* ===========================
//    SAVE DRAWING
// =========================== */

// export const saveDrawing = async (
//     pageId,
//     data
// ) => {

//     const response = await axios.put(
//         `${API_URL}/pages/${pageId}/drawing`,
//         data,
//         {
//             headers: authHeader()
//         }
//     );

//     return response.data;

// };

// /* ===========================
//    FINALIZE
// =========================== */

// export const finalizeDrawing = async (
//     pageId,
//     expectedVersion
// ) => {

//     const response = await axios.patch(
//         `${API_URL}/pages/${pageId}/drawing/finalize`,
//         {
//             expectedVersion
//         },
//         {
//             headers: authHeader()
//         }
//     );

//     return response.data;

// };

// /* ===========================
//    VERSION HISTORY
// =========================== */

// export const getRevisions = async (
//     pageId
// ) => {

//     const response = await axios.get(
//         `${API_URL}/pages/${pageId}/drawing/revisions`,
//         {
//             headers: authHeader()
//         }
//     );

//     return response.data;

// };

// /* ===========================
//    RESTORE
// =========================== */

// export const restoreRevision = async (
//     pageId,
//     revisionId,
//     expectedVersion
// ) => {

//     const response = await axios.patch(
//         `${API_URL}/pages/${pageId}/drawing/revisions/${revisionId}/restore`,
//         {
//             expectedVersion
//         },
//         {
//             headers: authHeader()
//         }
//     );

//     return response.data;

// };
