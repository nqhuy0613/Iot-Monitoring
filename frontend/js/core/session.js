"use strict";

function restoreSession() {
  const persistentToken = localStorage.getItem("iotToken");
  const storage = persistentToken ? localStorage : sessionStorage;
  const temporaryToken = sessionStorage.getItem("iotToken");
  const token = persistentToken || temporaryToken;
  if (!token) return;
  try {
    appState.token = token;
    appState.user = JSON.parse(storage.getItem("iotUser") || "null");
  } catch {
    clearSession();
  }
}

function saveSession(remember) {
  clearSession();
  const storage = remember ? localStorage : sessionStorage;
  storage.setItem("iotToken", appState.token);
  storage.setItem("iotUser", JSON.stringify(appState.user));
}

function clearSession() {
  [localStorage, sessionStorage].forEach(storage => {
    storage.removeItem("iotToken");
    storage.removeItem("iotUser");
  });
}

function logout() {
  appState.socket?.disconnect();
  clearSession();
  window.location.replace("index.html");
}

function applyAuthenticatedUser() {
  const fullName = appState.user.fullName || appState.user.username;
  $$('[data-user-name]').forEach(element => { element.textContent = fullName; });
}
