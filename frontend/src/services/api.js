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
    if (response.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    const error = new Error(
      (data && data.message) || `Yêu cầu thất bại (HTTP ${response.status})`,
    );
    error.response = { data, status: response.status };
    throw error;
  }
  return { data };
}

const api = {
  get: (url, config) => request("GET", url, null, config?.headers),
  post: (url, body, config) => request("POST", url, body, config?.headers),
  put: (url, body, config) => request("PUT", url, body, config?.headers),
  patch: (url, body, config) => request("PATCH", url, body, config?.headers),
  delete: (url, config) => request("DELETE", url, null, config?.headers),
};
export default api;
