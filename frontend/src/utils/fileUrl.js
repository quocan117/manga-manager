const API_BASE_URL = "http://localhost:8080";
export function resolveFileUrl(fileUrl) {
  if (!fileUrl) return null;
  return `${API_BASE_URL}/covers/${fileUrl}`;
}
export function isPreviewableFile(file) {
  if (file.previewable !== undefined) return file.previewable; 
  const ct = (file.contentType || "").toLowerCase();
  return ct.startsWith("image/") || ct === "application/pdf";
}
export default resolveFileUrl;