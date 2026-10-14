"use strict";

function initializeSensorHistory() {
  $("#sensor-filter-form").addEventListener("submit", event => {
    event.preventDefault();
    appState.sensorPage = 0;
    loadSensorHistory();
  });
  $("#sensor-page-size").addEventListener("change", event => {
    appState.sensorSize = Number(event.target.value);
    appState.sensorPage = 0;
    loadSensorHistory();
  });
  $("#sensor-sort").addEventListener("change", () => {
    appState.sensorPage = 0;
    loadSensorHistory();
  });
  loadSensorHistory();
}

async function loadSensorHistory() {
  const body = $("#sensor-history-body");
  body.innerHTML = emptyRow(4, "Đang tải dữ liệu...");
  const searchBy = $("#sensor-search-by").value;
  const value = $("#sensor-search-value").value.trim();
  if (value && !searchBy) {
    body.innerHTML = emptyRow(4, "Hãy chọn loại tìm kiếm khi đã nhập giá trị.");
    showToast("Nếu nhập value, bạn phải chọn searchBy.", "error");
    return;
  }

  const [sortBy, sortDirection] = $("#sensor-sort").value.split(":");
  const query = new URLSearchParams({
    page: appState.sensorPage,
    size: appState.sensorSize,
    sortBy,
    sortDirection
  });
  if (searchBy) query.set("searchBy", searchBy);
  if (value) query.set("value", value);

  try {
    const data = await apiFetch(`/api/sensor-data/history?${query}`);
    appState.sensorTotalPages = data.totalPages || 0;
    body.innerHTML = data.content?.length ? data.content.map(item => `<tr>
      <td>${item.id}</td><td>${formatSensorType(item.sensorType)}</td><td class="value-cell">${formatSensorValue(item.sensorType, item.value)}</td><td>${formatDateTime(item.createdAt)}</td>
    </tr>`).join("") : emptyRow(4, "Không có dữ liệu phù hợp.");
    renderPagination("sensor-pagination", appState.sensorPage, appState.sensorTotalPages, page => {
      appState.sensorPage = page;
      loadSensorHistory();
    });
  } catch (error) {
    body.innerHTML = emptyRow(4, error.message);
    renderPagination("sensor-pagination", 0, 0, () => {});
    showToast(error.message, "error");
  }
}
