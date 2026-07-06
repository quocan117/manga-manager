// Áp dụng kiến thức Exercise 10 (FER202): dùng Fetch API thay cho axios
// để thực hiện các lệnh gọi HTTP đến back-end.
const API_BASE_URL = "http://localhost:8080";
function buildHeaders(isFormData, customHeaders = {}) {
    const headers = { ...customHeaders };
    if (!isFormData && !headers["Content-Type"]) {
        headers["Content-Type"] = "application/json";
    }
    const token = localStorage.getItem("token");
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    // FormData tự sinh boundary, không được set Content-Type thủ công
    if (isFormData) {
        delete headers["Content-Type"];
    }
    return headers;
}
async function request(method, url, body, customHeaders) {
    const isFormData = body instanceof FormData;
    const response = await fetch(`${API_BASE_URL}${url}`, {
        method,
        headers: buildHeaders(isFormData, customHeaders),
        body: body ? (isFormData ? body : JSON.stringify(body)) : undefined,
    });
    const text = await response.text();
    let data = null;
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = text;
        }
    }
    if (!response.ok) {
        const error = new Error(
            (data && data.message) || `Yêu cầu thất bại (HTTP ${response.status})`,
        );
        error.response = { data, status: response.status };
        throw error;
    }
    return { data };
}
// Interface tương tự axios (get/post/put/patch/delete trả về { data })
// để các service khác trong dự án không cần thay đổi cách gọi.
const api = {
    get: (url, config) => request("GET", url, null, config?.headers),
    post: (url, body, config) => request("POST", url, body, config?.headers),
    put: (url, body, config) => request("PUT", url, body, config?.headers),
    patch: (url, body, config) => request("PATCH", url, body, config?.headers),
    delete: (url, config) => request("DELETE", url, null, config?.headers),
};
export default api;
