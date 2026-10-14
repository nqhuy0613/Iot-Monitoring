package iot.backend.iotmonitoring.controller;

import iot.backend.iotmonitoring.dto.request.DeviceActionHistoryFilterRequest;
import iot.backend.iotmonitoring.dto.response.DeviceActionHistoryPageResponse;
import iot.backend.iotmonitoring.service.device.DeviceActionHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/device-action")
@RequiredArgsConstructor
@Validated
public class DeviceActionController {

    private final DeviceActionHistoryService deviceActionHistoryService;

    @GetMapping("/history")
    public ResponseEntity<DeviceActionHistoryPageResponse> getDeviceActionHistory(
            @Valid @ModelAttribute DeviceActionHistoryFilterRequest filter
    ) {
        return ResponseEntity.ok(
                deviceActionHistoryService.getDeviceActionHistory(filter)
        );
    }
}
