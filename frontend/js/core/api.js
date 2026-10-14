"use strict";

async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers || {});
  headers.set("Authorization", `Bearer ${appState.token}`);
  if (options.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");

  let response;
  try {
    response = await fetch(`${APP_CONFIG.apiBaseUrl}${path}`, { ...options, headers });
  } catch {
    throw new Error("Không kết nối được backend tại localhost:8888.");
  }
  if (response.status === 401) {
    clearSession();
    window.location.replace("index.html");
    throw new Error("Phiên đăng nhập đã hết hạn.");
  }
  if (!response.ok) throw new Error(await readError(response, `Yêu cầu thất bại (${response.status}).`));
  return response.status === 204 ? null : response.json();
}

async function readError(response, fallback) {
  try {
    const body = await response.json();
    return body.message || body.detail || body.error || fallback;
  } catch {
    return fallback;
  }
}
