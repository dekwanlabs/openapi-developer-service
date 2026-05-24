package com.hesung.openapi.developer.domain.service;

import com.hesung.openapi.developer.controller.response.CapabilityDefinitionResponse;
import com.hesung.openapi.developer.controller.response.DeviceCapabilityResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OpenPlatformCapabilityService {

    List<CapabilityDefinitionResponse> listCapabilities();

    CapabilityDefinitionResponse getCapability(String code);

    Optional<CapabilityDefinitionResponse> findCapability(String code);

    List<String> inferCapabilityCodes(Map<String, Object> state);

    List<DeviceCapabilityResponse> toDeviceCapabilities(Map<String, Object> state);

    DeviceCapabilityResponse toDeviceCapability(String code);

    Map<String, Object> normalizeState(Map<String, Object> rawState);

    Map<String, Object> toDesired(Map<String, Object> standardDesired);

    String toRawField(String code);
}
