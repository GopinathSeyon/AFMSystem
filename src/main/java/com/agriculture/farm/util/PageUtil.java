package com.agriculture.farm.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtil {
    private PageUtil() {}

    public static Pageable of(int page, int size, String sortBy, String direction) {
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(p, s, sort);
    }
}
