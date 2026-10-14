"use strict";

function initializeDashboard() {
  appState.chartView = new window.SensorChart($("#sensorChart"));
  loadDashboard();
  connectRealtimeSocket();
}

async function loadDashboard() {
  window.clearTimeout(appState.dashboardRetryTimer);
  const [latestResult, chartResult, devicesResult] = await Promise.allSettled([
    apiFetch("/api/sensor-data/latest"),
    apiFetch("/api/sensor-data/chart"),
    apiFetch("/api/devices/status")
  ]);

  if (latestResult.status === "fulfilled") {
    renderLatestData(latestResult.value);
  }

  if (chartResult.status === "fulfilled") {
    const chart = chartResult.value;
    appState.chart = {
      temperature: chart.temperature || [],
      humidity: chart.humidity || [],
      light: chart.light || []
    };
    appState.chartView.update(appState.chart);
  }

  if (devicesResult.status === "fulfilled") {
    updateDeviceStatuses(devicesResult.value);
  } else {
    $("#device-list").innerHTML = '<p class="empty-message">Mất kết nối. Đang tự thử lại...</p>';
  }

  const failedResult = [latestResult, chartResult, devicesResult]
    .find(result => result.status === "rejected");
  if (failedResult) {
    if (!appState.dashboardLoadErrorNotified) {
      showToast(failedResult.reason?.message || "Không tải được dữ liệu Dashboard.", "error");
      appState.dashboardLoadErrorNotified = true;
    }
    appState.dashboardRetryTimer = window.setTimeout(loadDashboard, 5000);
  } else {
    appState.dashboardLoadErrorNotified = false;
  }
}

function renderLatestData(data) {
  $("#temperature-value").textContent = formatNumber(data.temperature);
  $("#humidity-value").textContent = formatNumber(data.humidity);
  $("#light-value").textContent = formatNumber(data.light);
  const text = data.createdAt ? `Updated ${formatDateTime(data.createdAt)}` : "Updated just now";
  $$('[data-latest-time]').forEach(element => { element.textContent = text; });
}

function updateDeviceStatuses(statuses) {
  (statuses || []).forEach(item => appState.devices.set(Number(item.deviceId), item.status));
  renderDevices();
}

function renderDevices() {
  $("#device-list").innerHTML = Object.entries(APP_CONFIG.deviceNames).map(([rawId, name]) => {
    const deviceId = Number(rawId);
    const status = appState.devices.get(deviceId) || "UNKNOWN";
    const isOn = status === "ON";
    const isPending = status === "LOADING" || appState.pendingDevices.has(deviceId);
    return `<div class="device-row">
      <div class="device-bulb"><i class="bx bx-bulb icon-sm"></i></div>
      <div class="device-copy"><p class="device-name">${escapeHtml(name)}</p><p class="device-status ${isOn ? "on" : ""} ${status === "FAILED" ? "failed" : ""}">Status: ${escapeHtml(status)}</p></div>
      <button class="switch ${isOn ? "on" : ""} ${isPending ? "loading" : ""}" type="button" data-device-id="${deviceId}" aria-label="${isOn ? "Tắt" : "Bật"} ${escapeHtml(name)}" ${isPending || status === "UNKNOWN" ? "disabled" : ""}></button>
    </div>`;
  }).join("");
  $$('.switch[data-device-id]').forEach(button => button.addEventListener("click", () => controlDevice(Number(button.dataset.deviceId))));
}

async function controlDevice(deviceId) {
  const oldStatus = appState.devices.get(deviceId);
  const action = oldStatus === "ON" ? "OFF" : "ON";
  const timer = window.setTimeout(() => {
    appState.pendingDevices.delete(deviceId);
    appState.devices.set(deviceId, oldStatus);
    renderDevices();
    showToast(`${APP_CONFIG.deviceNames[deviceId]} không phản hồi sau 5 giây.`, "error");
  }, 5000);
  appState.pendingDevices.set(deviceId, timer);
  appState.devices.set(deviceId, "LOADING");
  renderDevices();

  try {
    await apiFetch(`/api/devices/${deviceId}/control`, { method: "POST", body: JSON.stringify({ action }) });
    showToast(`Đã gửi lệnh ${action} đến ${APP_CONFIG.deviceNames[deviceId]}.`);
  } catch (error) {
    clearPendingDevice(deviceId);
    appState.devices.set(deviceId, oldStatus);
    renderDevices();
    showToast(error.message, "error");
  }
}

function connectRealtimeSocket() {
  appState.socket = new StompSocket(APP_CONFIG.webSocketUrl, {
    connected: () => setRealtimeState(true),
    disconnected: () => setRealtimeState(false)
  });
  appState.socket.subscribe("/topic/sensor-data", message => {
    renderLatestData(message);
    ["temperature", "humidity", "light"].forEach(key => {
      if (message[key] == null) return;
      appState.chart[key].push({ time: message.createdAt, value: message[key] });
      appState.chart[key] = appState.chart[key].slice(-15);
    });
    appState.chartView.update(appState.chart);
  });
  appState.socket.subscribe("/topic/devices/status", message => {
    const deviceId = Number(message.deviceId);
    clearPendingDevice(deviceId);
    appState.devices.set(deviceId, message.status);
    renderDevices();
    showToast(`${APP_CONFIG.deviceNames[deviceId]}: ${message.status}`, message.status === "FAILED" ? "error" : "success");
  });
  appState.socket.connect();
}

function setRealtimeState(isOnline) {
  const element = $("#realtime-status");
  if (!element) return;
  element.textContent = isOnline ? "Live" : "Offline";
  element.classList.toggle("online", isOnline);
}

function clearPendingDevice(deviceId) {
  const timer = appState.pendingDevices.get(deviceId);
  if (timer) window.clearTimeout(timer);
  appState.pendingDevices.delete(deviceId);
}
