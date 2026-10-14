package iot.backend.iotmonitoring.controller;

import iot.backend.iotmonitoring.dto.request.DeviceControlRequest;
import iot.backend.iotmonitoring.dto.response.DeviceControlResponse;
import iot.backend.iotmonitoring.dto.response.DeviceStatusResponse;
import iot.backend.iotmonitoring.entity.User;
import iot.backend.iotmonitoring.service.auth.CurrentUserService;
import iot.backend.iotmonitoring.service.device.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final CurrentUserService currentUserService;

    @GetMapping("/status")
    public ResponseEntity<List<DeviceStatusResponse>> getAllDeviceStatuses() {
        return ResponseEntity.ok(deviceService.getAllDeviceStatuses());
    }

    @PostMapping("/{deviceId}/control")
    public ResponseEntity<DeviceControlResponse> requestDeviceControl(
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceControlRequest request,
            Principal principal
    ) {
        User currentUser = currentUserService.requireCurrentUser(principal);
        return ResponseEntity.ok(deviceService.requestDeviceControl(
                deviceId,
                request.action(),
                currentUser
        ));
    }
}
