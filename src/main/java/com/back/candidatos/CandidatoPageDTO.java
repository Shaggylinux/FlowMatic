package com.back.candidatos;

import java.util.List;

public record CandidatoPageDTO(
    List<CandidatoListaDTO> data,
    int currentPage,
    int totalPages,
    long totalElements,
    int pageSize,
    int from,
    int to
) {}
