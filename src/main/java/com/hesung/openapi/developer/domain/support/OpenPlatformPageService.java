package com.hesung.openapi.developer.domain.support;

import com.hesung.openapi.developer.controller.request.PageQuery;
import com.hesung.openapi.developer.controller.response.PageResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class OpenPlatformPageService {

    public <T> PageResponse<T> paginate(List<T> source, PageQuery query) {
        List<T> records = source == null ? Collections.emptyList() : source;
        int current = query == null ? 1 : query.normalizedCurrent();
        int size = query == null ? 20 : query.normalizedSize();
        long total = records.size();
        long pages = total == 0 ? 0 : (total + size - 1) / size;
        int fromIndex = Math.max(0, (current - 1) * size);
        if (fromIndex >= records.size()) {
            return PageResponse.<T>builder()
                    .current(current)
                    .size(size)
                    .total(total)
                    .pages(pages)
                    .records(Collections.emptyList())
                    .build();
        }
        int toIndex = Math.min(fromIndex + size, records.size());
        return PageResponse.<T>builder()
                .current(current)
                .size(size)
                .total(total)
                .pages(pages)
                .records(records.subList(fromIndex, toIndex))
                .build();
    }
}
