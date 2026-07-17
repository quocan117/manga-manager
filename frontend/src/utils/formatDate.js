export function formatDateTime(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleString();
}

export function formatDateOnly(value) {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleDateString();
}

export function toBackendDateTime(datetimeLocalValue) {
  if (!datetimeLocalValue) return null;
  return `${datetimeLocalValue}:00.000`;
}
