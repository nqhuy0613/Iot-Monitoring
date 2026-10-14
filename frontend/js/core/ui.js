"use strict";

function renderPagination(elementId, currentPage, totalPages, changePage) {
  const container = $(`#${elementId}`);
  if (!container || totalPages === 0) {
    if (container) container.innerHTML = "";
    return;
  }
  const count = Math.min(5, totalPages);
  let start = Math.max(0, currentPage - 2);
  if (start + count > totalPages) start = totalPages - count;
  const pages = Array.from({ length: count }, (_, index) => start + index);
  container.innerHTML = `<button class="page-btn" type="button" data-page="${currentPage - 1}" ${currentPage === 0 ? "disabled" : ""}>«</button>
    ${pages.map(page => `<button class="page-btn ${page === currentPage ? "active" : ""}" type="button" data-page="${page}">${page + 1}</button>`).join("")}
    <button class="page-btn" type="button" data-page="${currentPage + 1}" ${currentPage >= totalPages - 1 ? "disabled" : ""}>»</button>`;
  $$('button:not(:disabled)', container).forEach(button => button.addEventListener("click", () => changePage(Number(button.dataset.page))));
}

function formatSensorType(type) { return ({ TEMPERATURE: "Temperature", HUMIDITY: "Humidity", LIGHT: "Light" })[type] || type || "--"; }
function formatSensorValue(type, value) { return `${formatNumber(value)} ${{ TEMPERATURE: "°C", HUMIDITY: "%", LIGHT: "lux" }[type] || ""}`.trim(); }
function formatNumber(value) { return value == null ? "--" : new Intl.NumberFormat("vi-VN", { maximumFractionDigits: 2 }).format(Number(value)); }
function formatDateTime(value) {
  const match = String(value || "").match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::(\d{2}))?/);
  return match ? `${match[3]}/${match[2]}/${match[1]} ${match[4]}:${match[5]}:${match[6] || "00"}` : value || "--";
}
function addQuery(query, name, value) { if (value !== "" && value != null) query.set(name, value); }
function emptyRow(columns, message) { return `<tr><td class="empty-cell" colspan="${columns}">${escapeHtml(message)}</td></tr>`; }
function escapeHtml(value) { return String(value ?? "").replace(/[&<>'"]/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[char]); }
function setButtonLoading(button, loading, text) { button.disabled = loading; button.textContent = text; }

function showToast(message, type = "") {
  const toast = $("#toast");
  if (!toast) return;
  window.clearTimeout(appState.toastTimer);
  toast.textContent = message;
  toast.className = `toast show ${type}`;
  appState.toastTimer = window.setTimeout(() => toast.classList.remove("show"), 3500);
}
