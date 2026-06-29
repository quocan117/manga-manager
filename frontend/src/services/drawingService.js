import axios from "axios";

const API_URL = "http://localhost:8080/mangaka";

const authHeader = () => ({
    Authorization: `Bearer ${localStorage.getItem("token")}`
});

/* ===========================
   GET DRAWING
=========================== */

export const getDrawing = async (pageId) => {

    const response = await axios.get(
        `${API_URL}/pages/${pageId}/drawing`,
        {
            headers: authHeader()
        }
    );

    return response.data;

};

/* ===========================
   SAVE DRAWING
=========================== */

export const saveDrawing = async (
    pageId,
    data
) => {

    const response = await axios.put(
        `${API_URL}/pages/${pageId}/drawing`,
        data,
        {
            headers: authHeader()
        }
    );

    return response.data;

};

/* ===========================
   FINALIZE
=========================== */

export const finalizeDrawing = async (
    pageId,
    expectedVersion
) => {

    const response = await axios.patch(
        `${API_URL}/pages/${pageId}/drawing/finalize`,
        {
            expectedVersion
        },
        {
            headers: authHeader()
        }
    );

    return response.data;

};

/* ===========================
   VERSION HISTORY
=========================== */

export const getRevisions = async (
    pageId
) => {

    const response = await axios.get(
        `${API_URL}/pages/${pageId}/drawing/revisions`,
        {
            headers: authHeader()
        }
    );

    return response.data;

};

/* ===========================
   RESTORE
=========================== */

export const restoreRevision = async (
    pageId,
    revisionId,
    expectedVersion
) => {

    const response = await axios.patch(
        `${API_URL}/pages/${pageId}/drawing/revisions/${revisionId}/restore`,
        {
            expectedVersion
        },
        {
            headers: authHeader()
        }
    );

    return response.data;

};