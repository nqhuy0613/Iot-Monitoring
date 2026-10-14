"use strict";

const APP_CONFIG = {
  apiBaseUrl: "http://localhost:8888",
  webSocketUrl: "ws://localhost:8888/ws",
  deviceNames: { 1: "Light 1", 2: "Light 2" }
};

const appState = {
  page: document.body.dataset.page,
  token: null,
  user: null,
  chart: { temperature: [], humidity: [], light: [] },
  chartView: null,
  devices: new Map(),
  pendingDevices: new Map(),
  sensorPage: 0,
  sensorSize: 10,
  sensorTotalPages: 0,
  historyPage: 0,
  historySize: 10,
  historyTotalPages: 0,
  socket: null,
  dashboardRetryTimer: null,
  dashboardLoadErrorNotified: false,
  toastTimer: null
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
