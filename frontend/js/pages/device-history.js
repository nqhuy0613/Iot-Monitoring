"use strict";

function initializeDeviceHistory() {
  bindTimeCopyButtons($("#device-history-body"));
  $("#device-history-filter-form").addEventListener("submit", event => {
    event.preventDefault();
    appState.historyPage = 0;
    loadDeviceHistory();
  });
  $("#history-page-size").addEventListener("change", event => {
    appState.historySize = Number(event.target.value);
    appState.historyPage = 0;
    loadDeviceHistory();
  });
  $("#history-sort").addEventListener("change", () => {
    appState.historyPage = 0;
    loadDeviceHistory();
  });
  loadDeviceHistory();
}

async function loadDeviceHistory() {
  const body = $("#device-history-body");
  body.innerHTML = emptyRow(6, "Đang tải dữ liệu...");
  const [sortBy, sortDirection] = $("#history-sort").value.split(":");
  const query = new URLSearchParams({
    page: appState.historyPage,
    size: appState.historySize,
    sortBy,
    sortDirection
  });
  addQuery(query, "time", $("#history-time").value.trim());
  addQuery(query, "deviceId", $("#history-device").value);
  addQuery(query, "action", $("#history-action").value);
  addQuery(query, "status", $("#history-status").value);

  try {
    const data = await apiFetch(`/api/device-action/history?${query}`);
    appState.historyTotalPages = data.totalPages || 0;
    body.innerHTML = data.content?.length ? data.content.map(item => `<tr>
      <td>${item.id}</td><td class="device-name-cell">${escapeHtml(item.deviceName)}</td><td class="${item.action === "ON" ? "action-on" : "action-off"}">${escapeHtml(item.action)}</td>
      <td><span class="badge ${String(item.status).toLowerCase()}">${escapeHtml(item.status)}</span></td><td>${escapeHtml(item.userName)}</td><td>${renderCopyableTime(item.createdAt)}</td>
    </tr>`).join("") : emptyRow(6, "Không có lịch sử điều khiển phù hợp.");
    renderPagination("history-pagination", appState.historyPage, appState.historyTotalPages, page => {
      appState.historyPage = page;
      loadDeviceHistory();
    });
  } catch (error) {
    body.innerHTML = emptyRow(6, error.message);
    renderPagination("history-pagination", 0, 0, () => {});
    showToast(error.message, "error");
  }
}
