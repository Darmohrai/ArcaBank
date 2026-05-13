package com.arcabank.core_finance.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages
) {}
