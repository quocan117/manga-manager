const API_BASE_URL = "http://localhost:8080";

export async function fetchSeriesFileBlob(fileId) {
  const token = localStorage.getItem("token");
  const response = await fetch(
    `${API_BASE_URL}/series-files/${fileId}/download`,
    {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    },
  );
  if (!response.ok) {
    throw new Error(`Không tải được file (HTTP ${response.status})`);
  }
  return response.blob();
}
