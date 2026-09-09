package dev.devwrapped.backend.metrics.dto;

import java.util.List;

public record ViewsSummary(long total, List<DailyCount> daily) {
}
