"use strict";

window.SensorChart = class SensorChart {
  constructor(canvas) {
    if (typeof window.Chart !== "function") {
      throw new Error("Không tải được thư viện Chart.js.");
    }

    this.chart = new window.Chart(canvas, {
      type: "line",
      data: {
        labels: [],
        datasets: [
          createSensorDataset("Temperature (°C)", "temperature", "#3182c7", "temperature"),
          createSensorDataset("Humidity (%)", "humidity", "#2aa26b", "humidity"),
          createSensorDataset("Light (lux)", "light", "#e96d16", "light")
        ]
      },
      // tùy chọn hiển thị
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        interaction: {
          mode: "index",
          intersect: false
        },
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            callbacks: {
              label: context => formatTooltipLabel(context)
            }
          }
        },
        // cấu hình trục x và y
        scales: {
          x: {
            grid: {
              display: false
            },
            border: {
              display: false
            },
            ticks: {
              color: "#7890aa",
              font: { size: 9 },
              autoSkip: true,
              maxTicksLimit: 7,
              maxRotation: 0
            }
          },
          temperature: {
            type: "linear",
            position: "left",
            min: 0,
            max: 50,
            grid: {
              color: "#dfe7ee"
            },
            border: {
              display: true,
              color: "#3182c7"
            },
            ticks: {
              color: "#3182c7",
              font: { size: 9 }
            },
            title: {
              display: true,
              text: "Temperature (°C)",
              color: "#3182c7",
              font: { size: 10, weight: "bold" }
            }
          },
          humidity: {
            type: "linear",
            position: "right",
            min: 0,
            max: 100,
            grid: {
              drawOnChartArea: false
            },
            border: {
              display: true,
              color: "#2aa26b"
            },
            ticks: {
              color: "#2aa26b",
              font: { size: 9 },
              callback: value => `${value}%`
            },
            title: {
              display: true,
              text: "Humidity (%)",
              color: "#2aa26b",
              font: { size: 10, weight: "bold" }
            }
          },
          light: {
            type: "linear",
            position: "right",
            beginAtZero: true,
            grid: {
              drawOnChartArea: false
            },
            border: {
              display: true,
              color: "#e96d16"
            },
            ticks: {
              color: "#e96d16",
              font: { size: 9 }
            },
            title: {
              display: true,
              text: "Light (lux)",
              color: "#e96d16",
              font: { size: 10, weight: "bold" }
            }
          }
        }
      }
    });
  }

  update(data) {
    const chartData = data || {};
    const longestSeries = [
      chartData.temperature || [],
      chartData.humidity || [],
      chartData.light || []
    ].reduce((longest, current) => current.length > longest.length ? current : longest, []);

    this.chart.data.labels = longestSeries.map(point => formatChartTime(point.time));
    this.chart.data.datasets.forEach(dataset => {
      dataset.data = (chartData[dataset.sensorKey] || []).map(point => {
        const value = Number(point.value);
        return Number.isFinite(value) ? value : null;
      });
    });
    this.chart.update("none");
  }
};

function createSensorDataset(label, sensorKey, color, yAxisID) {
  return {
    label,
    sensorKey,
    data: [],
    yAxisID,
    borderColor: color,
    backgroundColor: color,
    borderWidth: 2,
    pointRadius: 3,
    pointHoverRadius: 5,
    pointBorderWidth: 0,
    tension: 0,
    spanGaps: true
  };
}

function formatChartTime(value) {
  const match = String(value || "").match(/[T ](\d{2}):(\d{2})(?::(\d{2}))?/);
  return match ? `${match[1]}:${match[2]}:${match[3] || "00"}` : "--:--:--";
}

function formatTooltipLabel(context) {
  const value = context.parsed.y;
  const unit = context.dataset.sensorKey === "temperature"
    ? "°C"
    : context.dataset.sensorKey === "humidity" ? "%" : "lux";
  return `${context.dataset.label}: ${formatNumber(value)} ${unit}`;
}
