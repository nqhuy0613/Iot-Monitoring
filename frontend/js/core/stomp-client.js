"use strict";

class StompSocket {
  constructor(url, callbacks) {
    this.url = url;
    this.callbacks = callbacks;
    this.subscriptions = [];
    this.socket = null;
    this.closedByUser = false;
    this.reconnectTimer = null;
  }
  subscribe(destination, handler) { this.subscriptions.push({ destination, handler, id: `sub-${this.subscriptions.length + 1}` }); }
  connect() {
    this.closedByUser = false;
    try {
      this.socket = new WebSocket(this.url);
      this.socket.addEventListener("open", () => this.send("CONNECT", { "accept-version": "1.2", host: "localhost", "heart-beat": "0,0" }));
      this.socket.addEventListener("message", event => this.consume(String(event.data)));
      this.socket.addEventListener("close", () => this.onClose());
      this.socket.addEventListener("error", () => this.socket?.close());
    } catch { this.onClose(); }
  }
  consume(payload) {
    payload.split("\0").map(frame => frame.trim()).filter(Boolean).forEach(frame => {
      const separator = frame.indexOf("\n\n");
      const lines = (separator < 0 ? frame : frame.slice(0, separator)).split("\n");
      const command = lines.shift();
      const headers = Object.fromEntries(lines.filter(line => line.includes(":")).map(line => [line.slice(0, line.indexOf(":")), line.slice(line.indexOf(":") + 1)]));
      const body = separator < 0 ? "" : frame.slice(separator + 2);
      if (command === "CONNECTED") {
        this.subscriptions.forEach(item => this.send("SUBSCRIBE", { id: item.id, destination: item.destination, ack: "auto" }));
        this.callbacks.connected?.();
      }
      if (command === "MESSAGE") {
        const item = this.subscriptions.find(subscription => subscription.id === headers.subscription || subscription.destination === headers.destination);
        if (item) try { item.handler(JSON.parse(body)); } catch { /* Ignore malformed messages. */ }
      }
    });
  }
  send(command, headers = {}, body = "") {
    if (this.socket?.readyState !== WebSocket.OPEN) return;
    const headerText = Object.entries(headers).map(([key, value]) => `${key}:${value}`).join("\n");
    this.socket.send(`${command}\n${headerText}\n\n${body}\0`);
  }
  onClose() {
    this.callbacks.disconnected?.();
    if (!this.closedByUser) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = window.setTimeout(() => this.connect(), 3000);
    }
  }
  disconnect() {
    this.closedByUser = true;
    window.clearTimeout(this.reconnectTimer);
    if (this.socket?.readyState === WebSocket.OPEN) this.send("DISCONNECT");
    this.socket?.close();
  }
}
