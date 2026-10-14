"use strict";

window.SensorChart = class SensorChart {
  constructor(canvas) {
    this.canvas = canvas;
    this.context = canvas.getContext("2d");
    this.data = { temperature: [], humidity: [], light: [] };
    this.resizeObserver = new ResizeObserver(() => this.draw());
    this.resizeObserver.observe(canvas.parentElement);
  }

  update(data) {
    this.data = data;
    this.draw();
  }

  draw() {
    const bounds = this.canvas.parentElement.getBoundingClientRect();
    const ratio = Math.max(1, window.devicePixelRatio || 1);
    const width = Math.max(320, Math.floor(bounds.width));
    const height = Math.max(180, Math.floor(bounds.height));
    this.canvas.width = width * ratio;
    this.canvas.height = height * ratio;
    this.canvas.style.width = `${width}px`;
    this.canvas.style.height = `${height}px`;
    const context = this.context;
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    context.clearRect(0, 0, width, height);

    const margin = { top: 12, right: 12, bottom: 28, left: 34 };
    const plotWidth = width - margin.left - margin.right;
    const plotHeight = height - margin.top - margin.bottom;
    context.font = "9px Inter, Segoe UI, sans-serif";
    context.textAlign = "right";
    context.textBaseline = "middle";

    [120, 100, 80, 60, 40, 10].forEach((label, index, labels) => {
      const y = margin.top + plotHeight * index / (labels.length - 1);
      context.strokeStyle = "#dfe7ee";
      context.lineWidth = 1;
      context.beginPath();
      context.moveTo(margin.left, y);
      context.lineTo(width - margin.right, y);
      context.stroke();
      context.fillStyle = "#71879c";
      context.fillText(String(label), margin.left - 7, y);
    });

    const definitions = [
      ["temperature", "#3182c7"],
      ["humidity", "#2aa26b"],
      ["light", "#e96d16"]
    ];
    definitions.forEach(([key, color]) => this.drawSeries(this.data[key] || [], color, margin, plotWidth, plotHeight));

    const longest = definitions.map(([key]) => this.data[key] || []).sort((a, b) => b.length - a.length)[0];
    const labels = chooseLabels(longest, Math.min(7, longest.length));
    context.textAlign = "center";
    context.textBaseline = "bottom";
    context.fillStyle = "#7890aa";
    labels.forEach((point, index) => {
      const x = margin.left + plotWidth * index / Math.max(1, labels.length - 1);
      context.fillText(formatChartTime(point?.time), x, height - 2);
    });
  }

  drawSeries(points, color, margin, plotWidth, plotHeight) {
    if (!points.length) return;
    const values = points.map(point => Number(point.value)).filter(Number.isFinite);
    const min = Math.min(...values);
    const max = Math.max(...values);
    const span = max - min || Math.max(Math.abs(max), 1);
    const coordinates = points.map((point, index) => ({
      x: margin.left + plotWidth * index / Math.max(1, points.length - 1),
      y: margin.top + plotHeight - (((Number(point.value) - min) / span) * plotHeight * .66 + plotHeight * .15)
    }));
    const context = this.context;
    context.strokeStyle = color;
    context.lineWidth = 2;
    context.lineJoin = "round";
    context.beginPath();
    coordinates.forEach((point, index) => index ? context.lineTo(point.x, point.y) : context.moveTo(point.x, point.y));
    context.stroke();
    coordinates.forEach(point => {
      context.beginPath();
      context.fillStyle = color;
      context.arc(point.x, point.y, 3, 0, Math.PI * 2);
      context.fill();
    });
  }
};

function chooseLabels(points, count) {
  if (!count) return [];
  return Array.from({ length: count }, (_, index) => points[Math.round(index * (points.length - 1) / Math.max(1, count - 1))]);
}

function formatChartTime(value) {
  const match = String(value || "").match(/T(\d{2}):(\d{2})/);
  return match ? `${match[1]}:${match[2]}` : "--:--";
}
