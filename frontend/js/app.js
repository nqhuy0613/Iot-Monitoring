"use strict";

document.addEventListener("DOMContentLoaded", initializePage);

function initializePage() {
  bindCommonEvents();
  restoreSession();

  if (appState.page === "login") {
    initializeLoginPage();
    return;
  }
  if (!appState.token || !appState.user) {
    window.location.replace("index.html");
    return;
  }

  applyAuthenticatedUser();
  if (appState.page === "dashboard") initializeDashboard();
  if (appState.page === "sensor-data") initializeSensorHistory();
  if (appState.page === "device-history") initializeDeviceHistory();
}

function bindCommonEvents() {
  $$('[data-toast]').forEach(element => element.addEventListener("click", () => showToast(element.dataset.toast)));
  $$('[data-logout]').forEach(element => element.addEventListener("click", logout));
}
