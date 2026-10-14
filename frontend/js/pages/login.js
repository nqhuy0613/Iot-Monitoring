"use strict";

function initializeLoginPage() {
  if (appState.token && appState.user) {
    window.location.replace("dashboard.html");
    return;
  }
  $("#login-form").addEventListener("submit", handleLogin);
  $("#password-toggle").addEventListener("click", () => {
    const password = $("#password");
    const isHidden = password.type === "password";
    password.type = isHidden ? "text" : "password";
    $("#password-toggle i").className = isHidden ? "bx bx-hide icon-sm" : "bx bx-show icon-sm";
    $("#password-toggle").setAttribute("aria-label", isHidden ? "Ẩn mật khẩu" : "Hiện mật khẩu");
  });
}

async function handleLogin(event) {
  event.preventDefault();
  const username = $("#username").value.trim();
  const password = $("#password").value;
  const errorElement = $("#login-error");
  const button = $("#login-button");
  errorElement.textContent = "";

  if (!username || !password) {
    errorElement.textContent = "Vui lòng nhập đầy đủ username và password.";
    return;
  }

  setButtonLoading(button, true, "Logging in...");
  try {
    const response = await fetch(`${APP_CONFIG.apiBaseUrl}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    if (!response.ok) throw new Error(await readError(response, "Username hoặc password không đúng."));
    const data = await response.json();
    appState.token = data.token;
    appState.user = data.user;
    saveSession($("#remember-me").checked);
    window.location.replace("dashboard.html");
  } catch (error) {
    errorElement.textContent = error.message === "Failed to fetch"
      ? "Không kết nối được backend tại localhost:8888."
      : error.message;
  } finally {
    setButtonLoading(button, false, "Login");
  }
}
