package com.hesung.openapi.developer.controller.request;

import lombok.Data;

@Data
public class PageQuery {

    private static final int DEFAULT_CURRENT = 1;

    private static final int DEFAULT_SIZE = 20;

    private static final int MAX_SIZE = 100;

    private Integer current = DEFAULT_CURRENT;

    private Integer size = DEFAULT_SIZE;

    public int normalizedCurrent() {
        return current == null || current < 1 ? DEFAULT_CURRENT : current;
    }

    public int normalizedSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
