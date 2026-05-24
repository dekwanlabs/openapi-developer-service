package com.hesung.openapi.developer.infrastructure.remote.device;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "hsas-openapi-device-api",
        url = "${openapi.device-service.url}"
)
public interface DeviceOpenapiClient {

    @GetMapping("/v2/device/list")
    List<Map<String, Object>> listDevices(@RequestHeader(value = "Application-Context", required = false) String applicationContext);

    @GetMapping("/v2/device/state")
    Object getDeviceState(@RequestHeader(value = "Application-Context", required = false) String applicationContext,
                          @RequestParam("deviceSn") String deviceSn);

    @PostMapping("/v2/device/control")
    Boolean controlDevice(@RequestHeader(value = "Application-Context", required = false) String applicationContext,
                          @RequestBody Map<String, Object> payload);
}
