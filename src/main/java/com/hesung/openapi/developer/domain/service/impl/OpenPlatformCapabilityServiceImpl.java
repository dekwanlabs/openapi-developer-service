package com.hesung.openapi.developer.domain.service.impl;

import com.hesung.openapi.developer.controller.response.CapabilityDefinitionResponse;
import com.hesung.openapi.developer.controller.response.DeviceCapabilityResponse;
import com.hesung.openapi.developer.domain.service.OpenPlatformCapabilityService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OpenPlatformCapabilityServiceImpl implements OpenPlatformCapabilityService {

    private final List<CapabilityDefinitionResponse> capabilities = Arrays.asList(
            CapabilityDefinitionResponse.builder().code("power").name("Power").type("boolean").description("Turn device on or off").readable(true).writable(true).build(),
            CapabilityDefinitionResponse.builder().code("mode").name("Mode").type("enum").description("Device operating mode").readable(true).writable(true)
                    .values(Arrays.asList("normal", "sleep", "auto")).build(),
            CapabilityDefinitionResponse.builder().code("fan_speed").name("Fan Speed").type("integer").description("Fan speed level").readable(true).writable(true).unit("level").min(1).max(9).step(1).build(),
            CapabilityDefinitionResponse.builder().code("swing").name("Swing").type("enum").description("Oscillation mode").readable(true).writable(true)
                    .values(Arrays.asList("off", "horizontal", "vertical", "both")).build(),
            CapabilityDefinitionResponse.builder().code("temperature_setpoint").name("Temperature Setpoint").type("integer").description("Target temperature").readable(true).writable(true).unit("celsius").build(),
            CapabilityDefinitionResponse.builder().code("humidity_setpoint").name("Humidity Setpoint").type("integer").description("Target humidity").readable(true).writable(true).unit("percent").build(),
            CapabilityDefinitionResponse.builder().code("child_lock").name("Child Lock").type("boolean").description("Child lock switch").readable(true).writable(true).build(),
            CapabilityDefinitionResponse.builder().code("display").name("Display").type("boolean").description("Panel display light switch").readable(true).writable(true).build(),
            CapabilityDefinitionResponse.builder().code("sound").name("Sound").type("boolean").description("Panel sound switch").readable(true).writable(true).build(),
            CapabilityDefinitionResponse.builder().code("ambient_light").name("Ambient Light").type("boolean").description("Ambient light switch").readable(true).writable(true).build()
    );

    @Override
    public List<CapabilityDefinitionResponse> listCapabilities() {
        return capabilities;
    }

    @Override
    public CapabilityDefinitionResponse getCapability(String code) {
        return findCapability(code).orElse(null);
    }

    @Override
    public Optional<CapabilityDefinitionResponse> findCapability(String code) {
        String normalizedCode = normalizeCode(code);
        return capabilities.stream()
                .filter(item -> item.getCode().equals(normalizedCode))
                .findFirst();
    }

    @Override
    public List<String> inferCapabilityCodes(Map<String, Object> state) {
        if (state == null || state.isEmpty()) {
            return capabilities.stream().map(CapabilityDefinitionResponse::getCode).limit(4).collect(Collectors.toList());
        }
        return normalizeState(state).keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<DeviceCapabilityResponse> toDeviceCapabilities(Map<String, Object> state) {
        List<String> keys = inferCapabilityCodes(state);
        return keys.stream().map(this::toDeviceCapability).collect(Collectors.toList());
    }

    @Override
    public DeviceCapabilityResponse toDeviceCapability(String code) {
        String normalizedCode = normalizeCode(code);
        CapabilityDefinitionResponse definition = getCapability(normalizedCode);
        switch (normalizedCode) {
            case "fan_speed":
                return DeviceCapabilityResponse.builder()
                        .code(normalizedCode)
                        .type("integer")
                        .readable(true)
                        .writable(true)
                        .min(1)
                        .max(9)
                        .step(1)
                        .build();
            case "mode":
                return DeviceCapabilityResponse.builder()
                        .code(normalizedCode)
                        .type("enum")
                        .readable(true)
                        .writable(true)
                        .values(Arrays.asList("normal", "sleep", "auto"))
                        .build();
            case "swing":
                return DeviceCapabilityResponse.builder()
                        .code(normalizedCode)
                        .type("enum")
                        .readable(true)
                        .writable(true)
                        .values(Arrays.asList("off", "horizontal", "vertical", "both"))
                        .build();
            default:
                return DeviceCapabilityResponse.builder()
                        .code(normalizedCode)
                        .type(definition == null ? "boolean" : definition.getType())
                        .readable(definition == null ? true : definition.getReadable())
                        .writable(definition == null ? true : definition.getWritable())
                        .values(definition == null ? Collections.emptyList() : definition.getValues())
                        .build();
        }
    }

    @Override
    public Map<String, Object> normalizeState(Map<String, Object> rawState) {
        Map<String, Object> state = new LinkedHashMap<>();
        if (rawState == null || rawState.isEmpty()) {
            return state;
        }
        putIfPresent(state, "power", rawState, "power", "poweron");
        putIfPresent(state, "mode", rawState, "mode");
        putIfPresent(state, "fan_speed", rawState, "fan_speed", "speed");
        putIfPresent(state, "swing", rawState, "swing", "oscmode", "oscillate");
        putIfPresent(state, "temperature_setpoint", rawState, "temperature_setpoint", "temperature", "templevel");
        putIfPresent(state, "humidity_setpoint", rawState, "humidity_setpoint", "humidity");
        putIfPresent(state, "child_lock", rawState, "child_lock", "childlockon");
        putIfPresent(state, "display", rawState, "display", "lighton");
        putIfPresent(state, "sound", rawState, "sound", "muteon");
        putIfPresent(state, "ambient_light", rawState, "ambient_light", "ambient_lighton");
        return state;
    }

    @Override
    public Map<String, Object> toDesired(Map<String, Object> standardDesired) {
        Map<String, Object> desired = new LinkedHashMap<>();
        if (standardDesired == null) {
            return desired;
        }
        for (Map.Entry<String, Object> entry : standardDesired.entrySet()) {
            String rawField = toRawField(entry.getKey());
            if (StringUtils.hasText(rawField)) {
                desired.put(rawField, entry.getValue());
            }
        }
        return desired;
    }

    @Override
    public String toRawField(String code) {
        switch (normalizeCode(code)) {
            case "power":
                return "poweron";
            case "fan_speed":
                return "speed";
            case "swing":
                return "oscmode";
            case "temperature_setpoint":
                return "temperature";
            case "humidity_setpoint":
                return "humidity";
            case "child_lock":
                return "childlockon";
            case "display":
                return "lighton";
            case "sound":
                return "muteon";
            case "ambient_light":
                return "ambient_lighton";
            case "mode":
                return "mode";
            default:
                return null;
        }
    }

    private void putIfPresent(Map<String, Object> state, String standardCode, Map<String, Object> rawState, String... candidates) {
        for (String candidate : candidates) {
            if (rawState.containsKey(candidate)) {
                state.put(standardCode, rawState.get(candidate));
                return;
            }
        }
    }

    private String normalizeCode(String code) {
        return code == null ? "" : code.trim().toLowerCase();
    }
}
