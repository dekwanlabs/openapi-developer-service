package com.hesung.openapi.developer.service;

import com.hesung.openapi.developer.exception.OpenPlatformDeveloperBizExceptions;
import com.hesung.openapi.developer.exception.OpenPlatformDeveloperErrorCode;
import com.hesung.openapi.developer.model.CapabilityDef;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Standard capability registry.
 *
 * Each capability maps a public code (e.g. "power") to an internal
 * device field (e.g. "desired.power"), with type and access metadata.
 *
 * The registry is hardcoded for now. In production it would be
 * sourced from a database or Apollo config.
 */
@Service
public class CapabilityService {

    // ── Registry ────────────────────────────────────────────────

    private static final List<CapabilityDef> REGISTRY = Collections.unmodifiableList(Arrays.asList(
            CapabilityDef.builder().code("power").name("Power").dataType("boolean")
                    .access("readwrite").internalField("desired.power").build(),
            CapabilityDef.builder().code("fan_speed").name("Fan Speed").dataType("number")
                    .access("readwrite").internalField("desired.fan_speed").build(),
            CapabilityDef.builder().code("mode").name("Mode").dataType("string")
                    .access("readwrite").internalField("desired.mode")
                    .enumValues(Arrays.asList("normal", "natural", "sleep", "auto")).build(),
            CapabilityDef.builder().code("swing").name("Swing").dataType("boolean")
                    .access("readwrite").internalField("desired.swing").build(),
            CapabilityDef.builder().code("temperature").name("Temperature").dataType("number")
                    .access("readwrite").internalField("desired.temperature").build(),
            CapabilityDef.builder().code("humidity").name("Humidity").dataType("number")
                    .access("read").internalField("reported.humidity").build(),
            CapabilityDef.builder().code("online").name("Online").dataType("boolean")
                    .access("read").internalField("reported.online").build(),
            CapabilityDef.builder().code("filter_life").name("Filter Life").dataType("number")
                    .access("read").internalField("reported.filter_life").build()
    ));

    // ── Public API ──────────────────────────────────────────────

    public List<CapabilityDef> listAll() {
        return REGISTRY;
    }

    public Optional<CapabilityDef> findByCode(String code) {
        return REGISTRY.stream().filter(c -> c.getCode().equals(code)).findFirst();
    }

    public CapabilityDef requireByCode(String code) {
        return findByCode(code).orElseThrow(() ->
                OpenPlatformDeveloperBizExceptions.of(
                        OpenPlatformDeveloperErrorCode.DEVICE_CAPABILITY_UNSUPPORTED,
                        OpenPlatformDeveloperErrorCode.DEVICE_CAPABILITY_UNSUPPORTED.getErrorMessage() + ": " + code
                ));
    }

    /**
     * Converts raw internal device state to standard capability codes.
     *
     * Input:  {"desired.power": true, "reported.humidity": 45, ...}
     * Output: {"power": true, "humidity": 45, ...}
     */
    public Map<String, Object> normalizeState(Map<String, Object> rawState) {
        if (rawState == null || rawState.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (CapabilityDef cap : REGISTRY) {
            Object value = rawState.get(cap.getInternalField());
            if (value != null) {
                normalized.put(cap.getCode(), value);
            }
        }
        return normalized;
    }

    /**
     * Converts standard capability commands to internal device desired fields.
     *
     * Input:  {"power": true, "fan_speed": 3}
     * Output: {"desired.power": true, "desired.fan_speed": 3}
     */
    public Map<String, Object> toDesired(Map<String, Object> standardCommands) {
        if (standardCommands == null || standardCommands.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> desired = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : standardCommands.entrySet()) {
            CapabilityDef cap = requireByCode(entry.getKey());
            desired.put(cap.getInternalField(), entry.getValue());
        }
        return desired;
    }
}
