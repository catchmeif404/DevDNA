package dev.devwrapped.backend.metrics.dto;

public record MetricsSummaryResponse(Long memberCount, ViewsSummary views, ActiveUsersSummary activeUsers) {
}
