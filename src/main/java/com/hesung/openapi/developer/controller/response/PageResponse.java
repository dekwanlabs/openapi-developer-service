package com.hesung.openapi.developer.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PageResponse<T> {

    private long current;

    private long size;

    private long total;

    private long pages;

    private List<T> records;
}
