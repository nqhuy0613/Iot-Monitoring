# IoT Monitoring Frontend Demo

Giao diện được hoàn thiện từ bộ trang HTML/CSS tĩnh ban đầu, sử dụng HTML, CSS và JavaScript thuần. Không cần cài thư viện frontend.

## Chạy thử

1. Khởi động MySQL, MQTT Broker và backend tại `http://localhost:8888`.
2. Mở PowerShell tại thư mục `frontend`.
3. Chạy:

   ```powershell
   node dev-server.js
   ```

4. Mở `http://localhost:5500` trên trình duyệt.
5. Đăng nhập bằng tài khoản đã có trong database. Với dữ liệu test hiện tại có thể dùng `testuser` / `test123`.

## Chức năng demo

- Đăng nhập bằng JWT và ghi nhớ phiên đăng nhập.
- Dashboard hiển thị dữ liệu cảm biến mới nhất, biểu đồ 15 điểm gần nhất và trạng thái thiết bị.
- Điều khiển ON/OFF, hiển thị LOADING và nhận trạng thái thực tế qua WebSocket.
- Tra cứu dữ liệu cảm biến theo loại hoặc thời gian, sắp xếp và phân trang.
- Tra cứu lịch sử điều khiển theo thời gian, thiết bị, hành động, trạng thái, sắp xếp và phân trang.
- Trang profile lấy tên người dùng đăng nhập và giữ bốn liên kết tĩnh.
- Tự kết nối lại WebSocket khi mất kết nối.

## Cấu hình

Địa chỉ backend, endpoint API và topic WebSocket nằm trong `js/app.js`. Giá trị mặc định:

- REST API: `http://localhost:8888`
- WebSocket/STOMP: `ws://localhost:8888/ws`
- Sensor topic: `/topic/sensor-data`
- Device topic: `/topic/devices/status`

Nếu chạy frontend bằng cách mở trực tiếp file HTML, một số trình duyệt có thể chặn request do chính sách bảo mật. Vì vậy nên chạy bằng `node dev-server.js` như hướng dẫn trên.
