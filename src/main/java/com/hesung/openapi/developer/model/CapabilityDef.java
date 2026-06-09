package com.hesung.openapi.developer.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Standard capability definition — the public contract that Dreo
 * exposes to third-party developers.
 */
@Getter
@Builder
public class CapabilityDef {

    /** Unique capability code, e.g. "power", "fan_speed". */
    private final String code;

    /** Human-readable name. */
    private final String name;

    /** Value type: boolean / number / string / enum. */
    private final String dataType;

    /** Access level: read / write / readwrite. */
    private final String access;

    /** Allowed values when dataType = enum. */
    private final List<Object> enumValues;

    /** Maps to the internal device field, e.g. "desired.power". */
    private final String internalField;
}
