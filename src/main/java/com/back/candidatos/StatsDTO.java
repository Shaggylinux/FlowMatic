package com.back.candidatos;

public record StatsDTO(
    long total,
    long nuevos,
    long enProceso,
    long contratados
) {}
