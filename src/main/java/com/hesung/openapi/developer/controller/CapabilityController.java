package com.hesung.openapi.developer.controller;

import com.hesung.openapi.developer.model.CallerContext;
import com.hesung.openapi.developer.model.CapabilityDef;
import com.hesung.openapi.developer.service.CapabilityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Standard capability definitions, state normalization, and
 * command-to-desired conversion. Called by hsas-open after
 * raw device data is fetched.
 */
@RestController
@RequestMapping("/open/capabilities")
public class CapabilityController {

    private final CapabilityService capabilityService;

    public CapabilityController(CapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    /** List all standard capabilities. */
    @GetMapping
    public List<CapabilityDef> list(CallerContext caller) {
        return capabilityService.listAll();
    }

    /** Get a single capability by code. */
    @GetMapping("/{code}")
    public CapabilityDef get(CallerContext caller, @PathVariable String code) {
        return capabilityService.requireByCode(code);
    }

    /**
     * Normalize raw device state to standard capability codes.
     * Input from hsas-open: {"desired.power": true, "reported.humidity": 45}
     * Output: {"power": true, "humidity": 45}
     */
    @PostMapping("/normalize")
    public Map<String, Object> normalize(CallerContext caller, @RequestBody Map<String, Object> rawState) {
        return capabilityService.normalizeState(rawState);
    }

    /**
     * Convert standard commands to internal desired fields.
     * Input: {"power": true, "fan_speed": 3}
     * Output: {"desired.power": true, "desired.fan_speed": 3}
     */
    @PostMapping("/to-desired")
    public Map<String, Object> toDesired(CallerContext caller, @RequestBody Map<String, Object> standardCommands) {
        return capabilityService.toDesired(standardCommands);
    }
}
