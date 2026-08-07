const API_BASE_URL = "http://localhost:8080";

export function resolveImageUrl(value, placeholder = null) {
  if (!value) return placeholder;
  if (
    value.startsWith("http://") ||
    value.startsWith("https://") ||
    value.startsWith("data:")
  ) {
    return value;
  }

  if (value.startsWith("/")) {
    return `${API_BASE_URL}${value}`;
  }

  return `${API_BASE_URL}/covers/${value}`;
}

export default resolveImageUrl;
